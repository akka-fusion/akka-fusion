package fusion.http.server

import java.io.File
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path, Paths}

import akka.http.scaladsl.model.headers
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives
import akka.http.scaladsl.server.PathMatcher.{Matched, Unmatched}
import akka.http.scaladsl.server.directives.FileInfo
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.util.Tuple
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString
import fusion.core.model.ApiResult
import fusion.http.AkkaHttpSourceQueue
import fusion.http.util.HttpUtils
import helloscala.common.exception.HSException

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.Future

trait AbstractRoute extends Directives with HttpDirectives with FileDirectives {

  def route: Route

  def createTempFileFunc(
      dir: java.nio.file.Path = Paths.get("/tmp"),
      prefix: String = "hongka-",
      suffix: String = ".tmp"): FileInfo => File =
    fileInfo => Files.createTempFile(dir, prefix, suffix).toFile

  implicit class ContentTypeRich(contentType: ContentType) {

    def charset: Charset = contentType.charsetOption.map(_.nioCharset()).getOrElse(StandardCharsets.UTF_8)
  }

  def logRequest(logger: com.typesafe.scalalogging.Logger): Directive0 =
    mapRequest { req =>
      def entity = req.entity match {
        case HttpEntity.Empty => ""
        case _                => "\n" + req.entity
      }

      logger.debug(s"""
                      |method: ${req.method.value}
                      |uri: ${req.uri}
                      |search: ${req.uri.rawQueryString}
                      |header: ${req.headers.mkString("\n        ")}$entity""".stripMargin)
      req
    }



  def generateHeaders: Directive1[Map[String, String]] =
    extractRequest.flatMap { request =>
      val headerMap = request.headers
        .map(header => header.name() -> header.value())
        .toMap
      if (true) provide(headerMap)
      else reject(ForbiddenRejection("User authentication failed"))
    }

  //  def extractPageInput: Directive1[PageInput] = extract { ctx =>
  //    val query = ctx.request.uri.query()
  //    val page = query
  //      .get("page")
  //      .flatMap(AsInt.unapply)
  //      .getOrElse(Page.DEFAULT_PAGE)
  //    val size =
  //      query.get("size").flatMap(AsInt.unapply).getOrElse(Page.DEFAULT_SIZE)
  //    PageInput(page, size, query.filterNot {
  //      case (name, _) => name == "page" || name == "size"
  //    }.toMap)
  //  }

  def notPathPrefixTest[L](pm: PathMatcher[L]): Directive0 =
    rawNotPathPrefixTest(Slash ~ pm)

  def rawNotPathPrefixTest[L](pm: PathMatcher[L]): Directive0 = {
    implicit val LIsTuple: Tuple[L] = pm.ev
    extract(ctx => pm(ctx.unmatchedPath)).flatMap {
      case Matched(v, values) ⇒
        println(s"notPathPrefixTest v: $v, values: $values")
        reject
      case Unmatched ⇒ pass
    }
  }

  def setNoCache: Directive0 =
    mapResponseHeaders(
      h =>
        h ++
          List(headers.`Cache-Control`(CacheDirectives.`no-store`, CacheDirectives.`no-cache`),
               headers.RawHeader("Pragma", "no-cache")))

  def completeOk: Route = complete(HttpEntity.Empty)

  def completeNotImplemented: Route = complete(StatusCodes.NotImplemented)

  def pathGet[L](pm: PathMatcher[L]): Directive[L] = path(pm) & get

  def pathPost[L](pm: PathMatcher[L]): Directive[L] = path(pm) & post

  def pathPut[L](pm: PathMatcher[L]): Directive[L] = path(pm) & put

  def pathDelete[L](pm: PathMatcher[L]): Directive[L] = path(pm) & delete

  def putEntity[T](um: FromRequestUnmarshaller[T]): Directive1[T] =
    put & entity(um)

  def postEntity[T](um: FromRequestUnmarshaller[T]): Directive1[T] =
    post & entity(um)

  def completionStageComplete(
      future: java.util.concurrent.CompletionStage[AnyRef],
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK): Route = {
    import scala.compat.java8.FutureConverters._
    val f: AnyRef => Route = objectComplete(_, needContainer, successCode)
    onSuccess(future.toScala).apply(f)
  }

  def futureComplete(
      future: Future[AnyRef],
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK
  ): Route = {
    val f: AnyRef => Route = objectComplete(_, needContainer, successCode)
    onSuccess(future).apply(f)
  }

  @tailrec
  final def objectComplete(
      obj: Any,
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK
  ): Route = {
    obj match {
      case Right(result) =>
        objectComplete(result, needContainer, successCode)

      case Left(e: HSException) =>
        objectComplete(e, needContainer, successCode)

      case Some(result) =>
        objectComplete(result, needContainer, successCode)

      case None =>
        complete(jsonEntity(StatusCodes.NotFound, "数据不存在"))

      case response: HttpResponse =>
        complete(response)

      case responseEntity: ResponseEntity =>
        complete(HttpResponse(successCode, entity = responseEntity))

      case status: StatusCode =>
        complete(status)

      case result =>
        import fusion.http.util.JacksonSupport._
        val resp = if (needContainer) ApiResult.success(result) else result
        complete((successCode, resp))
    }
  }

  def eitherComplete[T](either: Either[HSException, T]): Route = {
    either match {
      case Right(result) =>
        objectComplete(result)
      case Left(e) =>
        objectComplete(e)
    }
  }

  /**
   * REST API 转发代理
   *
   * @param uri 要转发的地址
   * @param sourceQueue AkkaHTTP 源连接队列
   * @return
   */
  def restApiProxy(uri: Uri)(implicit sourceQueue: AkkaHttpSourceQueue): Route =
    extractRequestContext { ctx =>
      val req = ctx.request
      val request = req.copy(uri = uri.withQuery(req.uri.query()))
      val future = HttpUtils.hostRequest(request)(sourceQueue.httpSourceQueue, ctx.executionContext)
      onSuccess(future) { response =>
        complete(response)
      }
    }

}
