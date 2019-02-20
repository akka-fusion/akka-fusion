package fusion.http.util

import java.nio.charset.{Charset, UnsupportedCharsetException}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy, QueueOfferResult}
import akka.util.ByteString
import com.fasterxml.jackson.databind.node.ArrayNode
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import fusion.core.constant.ConfigConstant
import fusion.http.HttpSourceQueue
import fusion.http.exception.HttpException
import helloscala.common.exception.HSException
import helloscala.common.jackson.Jackson
import helloscala.common.util.StringUtils

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

object HttpUtils extends StrictLogging {

  val AKKA_HTTP_ROUTES_DISPATCHER = "akka-http-routes-dispatcher"
  private[util] var customMediaTypes: Map[String, MediaType] = getDefaultMediaTypes(ConfigFactory.load())

  private def getDefaultMediaTypes(config: Config): Map[String, MediaType] = {
    val compressibles = Map("compressible" -> MediaType.Compressible,
                            "notcompressible" -> MediaType.NotCompressible,
                            "gzipped" -> MediaType.Gzipped).withDefaultValue(MediaType.NotCompressible)
    if (!config.hasPath(ConfigConstant.HTTP.CUSTOM_MEDIA_TYPES)) {
      Map()
    } else {
      config
        .getStringList(ConfigConstant.HTTP.CUSTOM_MEDIA_TYPES)
        .asScala
        .flatMap { line =>
          try {
            val Array(mediaType, binary, compress, extensions) = line.split(';')
            val mt = MediaType.custom(mediaType,
                                      binary.toBoolean,
                                      compressibles(compress),
                                      extensions.split(',').toList.map(_.trim).filter(_.nonEmpty))
            mt.fileExtensions.map(_ -> mt)
          } catch {
            case _: Throwable => Nil
          }
        }
        .toMap
    }
  }

  def forExtension(ext: String): Option[MediaType] = {
    MediaTypes.forExtensionOption(ext).orElse(customMediaTypes.get(ext))
  }

  def registerMediaType(mediaTypes: MediaType*): Unit = {
    customMediaTypes = customMediaTypes ++ mediaTypes.flatMap(mediaType => mediaType.fileExtensions.map(_ -> mediaType))
  }

  def dump(response: HttpResponse)(implicit mat: Materializer) {
    val future = Unmarshal(response.entity).to[String]
    val value = Await.result(future, 10.seconds)
    println(s"[$response]\n\t\t$value\n")
  }

  @inline
  def haveSuccess(status: StatusCode): Boolean = haveSuccess(status.intValue())

  @inline
  def haveSuccess(status: Int): Boolean = status >= 200 && status < 300

  def mapHttpResponse[R: ClassTag](response: HttpResponse)(
      implicit
      mat: Materializer,
      um: FromEntityUnmarshaller[R] = JacksonSupport.unmarshaller,
      ec: ExecutionContext = null
  ): Future[Either[HSException, R]] =
    if (HttpUtils.haveSuccess(response.status)) {
      Unmarshal(response.entity)
        .to[R]
        .map(v => Right(v))(if (ec eq null) mat.executionContext else ec)
    } else {
      mapHttpResponseError[R](response)
    }

  def mapHttpResponseList[R](
      response: HttpResponse
  )(
      implicit
      ev1: ClassTag[R],
      mat: Materializer,
      ec: ExecutionContext = null
  ): Future[Either[HSException, List[R]]] =
    if (HttpUtils.haveSuccess(response.status)) {
      Unmarshal(response.entity)
        .to[ArrayNode](JacksonSupport.unmarshaller, ec, mat)
        .map { array =>
          val list = array.asScala
            .map(
              node =>
                Jackson.defaultObjectMapper
                  .treeToValue(node, ev1.runtimeClass)
                  .asInstanceOf[R])
            .toList
          Right(list)
        }(if (ec eq null) mat.executionContext else ec)
    } else {
      mapHttpResponseError[List[R]](response)
    }

  def mapHttpResponseError[R](response: HttpResponse)(
      implicit
      mat: Materializer,
      ec: ExecutionContext = null
  ): Future[Either[HSException, R]] =
    if (response.entity.contentType.mediaType == MediaTypes.`application/json`) {
      Unmarshal(response.entity)
        .to[HSException](JacksonSupport.unmarshaller, ec, mat)
        .map(e => Left(e))(if (ec eq null) mat.executionContext else ec)
    } else {
      Unmarshal(response.entity)
        .to[String]
        .map(errMsg => Left(HttpException(response.status, errMsg)))(if (ec eq null) mat.executionContext else ec)
    }

  def queryToMap(request: HttpRequest): Map[String, String] =
    queryToMap(request.uri.query())

  def queryToMap(query: Uri.Query): Map[String, String] = query.toMap

  /**
   * 从 HTTP header Content-Type 中获取 charset
   *
   * @param ct HTTP header Content-Type 值
   * @return
   */
  def parseCharset(ct: String): Option[Charset] =
    try {
      if (StringUtils.isNoneBlank(ct) && ct.contains("=")) {
        val arr = ct.split('=')
        val cs = Charset.forName(arr.last)
        Option(cs)
      } else {
        None
      }
    } catch {
      case _: UnsupportedCharsetException =>
        None
    }

  /**
   * 根据 Content-Type 字符串解析转换成
   *
   * @param value Content-Type 字符串
   * @return
   */
  def parseContentType(value: String): Option[ContentType] = {
    // TODO akka-http 的ContentType/MediaType覆盖不够怎么办？

    var contentType = value
    var charset = ""
    if (StringUtils.isNoneBlank(contentType)) {
      val arr = contentType.split(';')
      contentType = arr(0)
      if (arr.length == 2) {
        val arr2 = arr(1).split('=')
        if (arr2.length == 2)
          charset = arr2(1).trim
      }
    }

    val tupleKey = contentType.split('/') match {
      case Array(k, v) => (k.toLowerCase(), v.toLowerCase())
      case Array(k)    => (k.toLowerCase(), "")
      case _           => ("", "")
    }
    logger.debug(s"tupleKey: $tupleKey")

    if (tupleKey._2.contains("powerpoint")) {
      Some(ContentType(MediaTypes.`application/vnd.ms-powerpoint`))
    } else if (tupleKey._2.contains("excel")) {
      Some(ContentType(MediaTypes.`application/vnd.ms-excel`))
    } else if (tupleKey._2.contains("msword")) {
      Some(ContentType(MediaTypes.`application/msword`))
    } else {
      tupleKeyToContentType(charset, tupleKey)
    }
  }

  private def tupleKeyToContentType(charset: String, tupleKey: (String, String)) = {
    val mediaType = MediaTypes
      .getForKey(tupleKey)
      .getOrElse(MediaTypes.`application/octet-stream`)
    val httpContentType: ContentType = mediaType match {
      case woc: MediaType.WithOpenCharset =>
        val httpCharset = HttpCharsets
          .getForKeyCaseInsensitive(charset)
          .getOrElse(HttpCharsets.`UTF-8`)
        woc.withCharset(httpCharset)
      case mt: MediaType.Binary           => ContentType(mt)
      case mt: MediaType.WithFixedCharset => ContentType(mt)
      case _                              => null
    }
    Option(httpContentType)
  }

  def cachedHostConnectionPool(url: String)(implicit system: ActorSystem, mat: Materializer): HttpSourceQueue = {
    val uri = Uri(url)
    uri.scheme match {
      case "http"  => cachedHostConnectionPool(uri.authority.host.address(), uri.authority.port)
      case "https" => cachedHostConnectionPoolHttps(uri.authority.host.address(), uri.authority.port)
      case _       => throw new IllegalArgumentException(s"URL: $url 不是有效的 http 或 https 协议")
    }
  }

  /**
   * 获取 CachedHostConnectionPool，当发送的url不包含 host 和 port 时将使用默认值
   *
   * @param host 默认host
   * @param port 默认port
   * @param mat  ActorMaterializer
   * @return
   */
  def cachedHostConnectionPool(host: String, port: Int)(
      implicit system: ActorSystem,
      mat: Materializer): HttpSourceQueue = {
    val poolClientFlow =
      Http().cachedHostConnectionPool[Promise[HttpResponse]](host, port)
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](512, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) => p.success(resp)
        case (Failure(e), p)    => p.failure(e)
      }))(Keep.left)
      .run()
  }

  /**
   * 获取 CachedHostConnectionPoolHttps，同[[cachedHostConnectionPool()]]，区别是使用HTTPs协议
   *
   * @param host 默认host
   * @param port 默认port
   * @param mat  ActorMaterializer
   * @return
   */
  def cachedHostConnectionPoolHttps(host: String, port: Int = 80)(
      implicit system: ActorSystem,
      mat: Materializer): HttpSourceQueue = {
    val poolClientFlow =
      Http().cachedHostConnectionPoolHttps[Promise[HttpResponse]](host, port)
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](512, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) => p.success(resp)
        case (Failure(e), p)    => p.failure(e)
      }))(Keep.left)
      .run()
  }

  /**
   * 发送 Http 请求
   *
   * @param method 请求方法类型
   * @param uri 请求地址
   * @param params 请求URL查询参数
   * @param data 请求数据（将备序列化成JSON）
   * @param headers 请求头
   * @param protocol HTTP协议版本
   * @return HttpResponse
   */
  def singleRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`)(implicit mat: ActorMaterializer): Future[HttpResponse] = {
    val request = HttpRequest(
      method,
      uri.withQuery(Uri.Query(uri.query() ++ params: _*)),
      headers,
      entity = data match {
        case null                    => HttpEntity.Empty
        case entity: UniversalEntity => entity
        case _                       => HttpEntity(ContentTypes.`application/json`, Jackson.stringify(data))
      },
      protocol = protocol
    )
    singleRequest(request)
  }

  /**
   * 发送 Http 请求
   *
   * @param request HttpRequest
   * @param mat ActorMaterializer
   * @return
   */
  def singleRequest(request: HttpRequest)(implicit mat: ActorMaterializer): Future[HttpResponse] =
    Http()(mat.system).singleRequest(request)

  /**
   * 发送 Http 请求，使用 CachedHostConnectionPool。见：[[cachedHostConnectionPool()]]
   *
   * @param request         HttpRequest
   * @param httpSourceQueue 使用了CachedHostConnectionPool的 HTTP 队列
   * @return Future[HttpResponse]
   */
  def hostRequest(
      request: HttpRequest)(implicit httpSourceQueue: HttpSourceQueue, ec: ExecutionContext): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    httpSourceQueue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => responsePromise.future
      case QueueOfferResult.Dropped =>
        Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed =>
        Future.failed(
          new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def hostRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil)(
      implicit httpSourceQueue: HttpSourceQueue,
      ec: ExecutionContext): Future[HttpResponse] = {
    val entity = if (data != null) {
      data match {
        case entity: RequestEntity => entity
        case _ =>
          HttpEntity(ContentTypes.`application/json`, Jackson.stringify(data))
      }
    } else {
      HttpEntity.Empty
    }
    hostRequest(HttpRequest(method, uri.withQuery(Uri.Query(uri.query() ++ params: _*)), headers, entity))
  }

  def makeRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, Any)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil): HttpRequest = {
    val entity = if (data != null) {
      data match {
        case entity: MessageEntity => entity
        case _                     => HttpEntity(ContentTypes.`application/json`, Jackson.stringify(data))
      }
    } else {
      HttpEntity.Empty
    }
    val httpParams = params.map { case (key, value) => key -> value.toString }
    HttpRequest(method, uri.withQuery(Uri.Query(httpParams: _*)), headers, entity)
  }

  def toStrictEntity(response: HttpResponse)(implicit mat: Materializer): HttpEntity.Strict =
    toStrictEntity(response.entity)

  def toByteString(response: HttpResponse)(implicit mat: ActorMaterializer): Future[ByteString] =
    Unmarshal(response.entity).to[ByteString]

  def toStrictEntity(responseEntity: ResponseEntity)(implicit mat: Materializer): HttpEntity.Strict = {
    import scala.concurrent.duration._
    val dr = 10.seconds
    val f = responseEntity.toStrict(dr)
    Await.result(f, dr)
  }

  def entityJson(string: String) = HttpEntity(ContentTypes.`application/json`, string)

}
