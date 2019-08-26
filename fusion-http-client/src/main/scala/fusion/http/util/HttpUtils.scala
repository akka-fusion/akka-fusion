/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.http.util

import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.security.KeyStore
import java.security.SecureRandom

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.model.Uri.Authority
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.`Timeout-Access`
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import com.typesafe.scalalogging.StrictLogging
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import fusion.common.constant.ConfigKeys
import fusion.core.http.headers.`X-Trace-Id`
import fusion.core.util.FusionUtils
import fusion.http.HttpSourceQueue
import fusion.json.jackson.Jackson
import helloscala.common.util.StringUtils
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import scala.util.control.NonFatal

object HttpUtils extends StrictLogging {
  val AKKA_HTTP_ROUTES_DISPATCHER = "akka-http-routes-dispatcher"

  val DEFAULT_PORTS: Map[String, Int] =
    Map(
      "ftp" -> 21,
      "ssh" -> 22,
      "telnet" -> 23,
      "smtp" -> 25,
      "domain" -> 53,
      "tftp" -> 69,
      "http" -> 80,
      "ws" -> 80,
      "pop3" -> 110,
      "nntp" -> 119,
      "imap" -> 143,
      "snmp" -> 161,
      "ldap" -> 389,
      "https" -> 443,
      "wss" -> 443,
      "imaps" -> 993,
      "nfs" -> 2049).withDefaultValue(-1)
  private[util] var customMediaTypes: Map[String, MediaType] = getDefaultMediaTypes(ConfigFactory.load())

  private def getDefaultMediaTypes(config: Config): Map[String, MediaType] = {
    val compressibles = Map(
      "compressible" -> MediaType.Compressible,
      "notcompressible" -> MediaType.NotCompressible,
      "gzipped" -> MediaType.Gzipped).withDefaultValue(MediaType.NotCompressible)
    if (!config.hasPath(ConfigKeys.HTTP.CUSTOM_MEDIA_TYPES)) {
      Map()
    } else {
      config
        .getStringList(ConfigKeys.HTTP.CUSTOM_MEDIA_TYPES)
        .asScala
        .flatMap { line =>
          try {
            val Array(mediaType, binary, compress, extensions) = line.split(';')
            val mt = MediaType.custom(
              mediaType,
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

  @inline def copyUri(request: HttpRequest, authority: String): HttpRequest = copyUri(request, "http", authority)

  @inline def copyUri(request: HttpRequest, scheme: String, authority: String): HttpRequest =
    copyUri(request, scheme, Authority.parse(authority))

  @inline def copyUri(request: HttpRequest, scheme: String, authority: Authority): HttpRequest =
    request.copy(uri = copyUri(request.uri, scheme, authority))

  @inline def copyUri(uri: Uri, scheme: String, authority: String): Uri =
    copyUri(uri, scheme, Authority.parse(authority))
  @inline def copyUri(uri: Uri, authority: String): Uri = copyUri(uri, "http", Authority.parse(authority))

  @inline def copyUri(uri: Uri, scheme: String, authority: Authority): Uri =
    uri.copy(scheme = scheme, authority = authority)

  @inline def clearAuthority(uri: Uri): Uri = uri.copy(scheme = "", authority = Authority.Empty)

  @inline def clearSchemaAndAuthority(uri: Uri): Uri = clearAuthority(uri)

  @inline def forExtension(ext: String): Option[MediaType] =
    MediaTypes.forExtensionOption(ext).orElse(customMediaTypes.get(ext))

  @inline def registerMediaType(mediaTypes: MediaType*): Unit = {
    customMediaTypes = customMediaTypes ++ mediaTypes.flatMap(mediaType => mediaType.fileExtensions.map(_ -> mediaType))
  }

  @inline def generateTraceHeader(): HttpHeader = {
    `X-Trace-Id`(FusionUtils.generateTraceId().toString)
  }

  def dump(response: HttpResponse)(implicit mat: Materializer) {
    val future = Unmarshal(response.entity).to[String]
    val value = Await.result(future, 10.seconds)
    println(s"[$response]\n\t\t$value\n")
  }

  @inline
  def haveSuccess(status: StatusCode): Boolean = is2xx(status.intValue())

  @inline
  def haveSuccess(status: Int): Boolean = is2xx(status)

  @inline
  def is2xx(status: StatusCode): Boolean = is2xx(status.intValue())

  @inline
  def is2xx(status: Int): Boolean = status >= 200 && status < 300

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

  private def tupleKeyToContentType(charset: String, tupleKey: (String, String)): Option[ContentType] = {
    val mediaType = MediaTypes.getForKey(tupleKey).getOrElse(MediaTypes.`application/octet-stream`)
    val httpContentType: ContentType = mediaType match {
      case woc: MediaType.WithOpenCharset =>
        val httpCharset = HttpCharsets.getForKeyCaseInsensitive(charset).getOrElse(HttpCharsets.`UTF-8`)
        woc.withCharset(httpCharset)
      case mt: MediaType.Binary           => ContentType(mt)
      case mt: MediaType.WithFixedCharset => ContentType(mt)
      case _                              => null
    }
    Option(httpContentType)
  }

  def cachedHostConnectionPool(uri: Uri, bufferSize: Int)(
      implicit system: ActorSystem,
      mat: Materializer): HttpSourceQueue = {
    uri.scheme match {
      case "http"  => cachedHostConnectionPool(uri.authority.host.address(), uri.effectivePort, bufferSize)
      case "https" => cachedHostConnectionPoolHttps(uri.authority.host.address(), uri.effectivePort, bufferSize)
      case _       => throw new IllegalArgumentException(s"URI: $uri 不是有效的 http 或 https 地址")
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
  def cachedHostConnectionPool(host: String, port: Int, bufferSize: Int)(
      implicit system: ActorSystem,
      mat: Materializer): HttpSourceQueue = {
    val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](host, port)
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) => p.success(resp)
        case (Failure(e), p)    => p.failure(e)
      }))(Keep.left)
      .run()
  }

  /**
   * 获取 CachedHostConnectionPoolHttps，同 [[cachedHostConnectionPool(String, Int, Int)]]，区别是使用HTTPs协议
   *
   * @param host 默认host
   * @param port 默认port
   * @param mat  ActorMaterializer
   * @return
   */
  def cachedHostConnectionPoolHttps(host: String, port: Int, bufferSize: Int)(
      implicit system: ActorSystem,
      mat: Materializer): HttpSourceQueue = {
    val poolClientFlow = Http().cachedHostConnectionPoolHttps[Promise[HttpResponse]](host, port)
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) => p.success(resp)
        case (Failure(e), p)    => p.failure(e)
      }))(Keep.left)
      .run()
  }

  def generateHttps(
      keyPassword: String,
      keystore: InputStream,
      keyStoreType: String = "PKCS12",
      algorithm: String = "SunX509",
      protocol: String = "TLS",
      akkaSslConfig: Option[AkkaSSLConfig] = None): HttpsConnectionContext = {
    var hcc: HttpsConnectionContext = null
    try {
      val password = keyPassword.toCharArray
      val ks = KeyStore.getInstance(keyStoreType)
      ks.load(keystore, password)

      val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance(algorithm)
      keyManagerFactory.init(ks, password)

      val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(algorithm)
      tmf.init(ks)

      val sslContext: SSLContext = SSLContext.getInstance(protocol)
      sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom())

      hcc = new HttpsConnectionContext(sslContext, akkaSslConfig)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        System.exit(-1)
    }
    hcc
  }

  def buildRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`): HttpRequest = {
    val entity = data match {
      case null                    => HttpEntity.Empty
      case entity: UniversalEntity => entity
      case _                       => HttpEntity(ContentTypes.`application/json`, Jackson.stringify(data))
    }
    HttpRequest(method, uri.withQuery(Uri.Query(uri.query() ++ params: _*)), headers, entity, protocol)
  }

  /**
   * 发送 Http 请求
   *
   * @param method   请求方法类型
   * @param uri      请求地址
   * @param params   请求URL查询参数
   * @param data     请求数据（将备序列化成JSON）
   * @param headers  请求头
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
    val request = buildRequest(method, uri, params, data, headers, protocol)
    singleRequest(request)
  }

  /**
   * 发送 Http 请求
   *
   * @param request HttpRequest
   * @param mat     ActorMaterializer
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

  def entityJson(status: StatusCode, msg: String): HttpEntity.Strict = entityJson(status.intValue(), msg)
  def entityJson(status: Int, msg: String): HttpEntity.Strict = entityJson(s"""{"status":$status,"msg":"$msg"}""")
  def entityJson(json: String): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, json)

  def logRequest(logger: com.typesafe.scalalogging.Logger): Directive0 = {
    Directives.mapRequest { req =>
      curlLogging(req)(logger)
    }
  }

  def curlLogging(req: HttpRequest)(implicit _log: Logger = null): HttpRequest = {
    val log = if (null == _log) logger else _log
    log.whenDebugEnabled {
      val entity = req.entity match {
        case HttpEntity.Empty => ""
        case _                => "\n" + req.entity
      }
      val headers = req.headers.filterNot(_.name() == `Timeout-Access`.name)
      log.debug(s"""HttpRequest
                |${req.protocol.value} ${req.method.value} ${req.uri}
                |search: ${toString(req.uri.query())}
                |header: ${headers.mkString("\n        ")}$entity""".stripMargin)
    }
    req
  }

  def curlLoggingResponse(req: HttpRequest, resp: HttpResponse, printResponseEntity: Boolean = false)(
      implicit _log: Logger = null): HttpResponse = {
    val log = if (null == _log) logger else _log
    log.whenDebugEnabled {
      val sb = new StringBuilder
      sb.append("HttpResponse").append("\n")
      sb.append(s"${resp.protocol.value} ${req.method.value} ${req.uri}").append("\n")
      sb.append(s"status: ${resp.status}").append("\n")
      sb.append("header: ")
      resp.headers.foreach(h => sb.append(h.toString()).append("\n        "))
      if (printResponseEntity) {
        sb.append(resp.entity match {
          case HttpEntity.Empty => ""
          case _                => "\n" + resp.entity
        })
      }
      log.debug(sb.toString())
    }
    resp
  }

  def jsonEntity(status: StatusCode, msg: String): (StatusCode, HttpEntity.Strict) =
    status -> HttpUtils.entityJson(s"""{"status":${status.intValue()},"msg":"$msg"}""")

  def jsonResponse(status: StatusCode, msg: String): HttpResponse =
    HttpResponse(status, entity = HttpUtils.entityJson(s"""{"status":${status.intValue()},"msg":"$msg"}"""))

  def toString(query: Uri.Query): String = query.map { case (name, value) => s"$name=$value" }.mkString("&")
}
