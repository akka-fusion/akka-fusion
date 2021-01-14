/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.http.server

import java.io.File
import java.nio.charset.{Charset, StandardCharsets}
import java.time.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime}

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher.{Matched, Unmatched}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FileInfo
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, FromStringUnmarshaller, Unmarshaller}
import akka.stream.scaladsl.{FileIO, Sink, Source}
import fusion.http.AkkaHttpSourceQueue
import fusion.http.rejection.ForbiddenRejection
import fusion.http.util.HttpUtils
import helloscala.common.util.TimeUtils

import scala.collection.immutable

trait HttpDirectives {

  implicit class ContentTypeRich(contentType: ContentType) {
    def charset: Charset = contentType.charsetOption.map(_.nioCharset()).getOrElse(StandardCharsets.UTF_8)
  }

  implicit def localDateFromStringUnmarshaller: FromStringUnmarshaller[LocalDate] =
    HttpDirectives._localDateFromStringUnmarshaller

  implicit def localTimeFromStringUnmarshaller: FromStringUnmarshaller[LocalTime] =
    HttpDirectives._localTimeFromStringUnmarshaller

  implicit def localDateTimeFromStringUnmarshaller: FromStringUnmarshaller[LocalDateTime] =
    HttpDirectives._localDateTimeFromStringUnmarshaller

  implicit def offsetDateTimeFromStringUnmarshaller: Unmarshaller[String, OffsetDateTime] =
    HttpDirectives._offsetDateTimeFromStringUnmarshaller

  def curlLogging(logger: com.typesafe.scalalogging.Logger): Directive0 =
    mapRequest { req =>
      def entity =
        req.entity match {
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

  def logRequest(logger: com.typesafe.scalalogging.Logger): Directive0 = HttpUtils.logRequest(logger)

  def generateHeaders: Directive1[Map[String, String]] =
    extractRequest.flatMap { request =>
      val headerMap = request.headers.map(header => header.name() -> header.value()).toMap
      if (true) provide(headerMap)
      else reject(ForbiddenRejection("User authentication failed"))
    }

  def notPathPrefixTest[L](pm: PathMatcher[L]): Directive0 =
    rawNotPathPrefixTest(Slash ~ pm)

  def rawNotPathPrefixTest[L](pm: PathMatcher[L]): Directive0 = {
    extract(ctx => pm(ctx.unmatchedPath)).flatMap {
      case Matched(v, values) => reject
      case Unmatched          => pass
    }
  }

  def setNoCache: Directive0 =
    mapResponseHeaders(h =>
      h ++
        List(
          headers.`Cache-Control`(CacheDirectives.`no-store`, CacheDirectives.`no-cache`),
          headers.RawHeader("Pragma", "no-cache")
        )
    )

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

  /**
   * Streams the bytes of the file submitted using multipart with the given field name into designated files on disk.
   * If there is an error writing to disk the request will be failed with the thrown exception, if there is no such
   * field the request will be rejected. Stored files are cleaned up on exit but not on failure.
   *
   * @group fileupload
   */
  def uploadedFiles(destFn: FileInfo => File): Directive1[immutable.Seq[(FileInfo, File)]] =
    entity(as[Multipart.FormData]).flatMap { formData =>
      extractRequestContext.flatMap { ctx =>
        implicit val mat = ctx.materializer
        implicit val ec = ctx.executionContext

        val uploaded: Source[(FileInfo, File), Any] = formData.parts
        //          .mapConcat { part =>
        //            if (part.filename.isDefined && part.name == fieldName) part :: Nil
        //            else {
        //              part.entity.discardBytes()
        //              Nil
        //            }
        //          }
          .mapAsync(1) { part =>
            val fileInfo = FileInfo(part.name, part.filename.get, part.entity.contentType)
            val dest = destFn(fileInfo)

            part.entity.dataBytes.runWith(FileIO.toPath(dest.toPath)).map { _ =>
              (fileInfo, dest)
            }
          }

        val uploadedF = uploaded.runWith(Sink.seq[(FileInfo, File)])

        onSuccess(uploadedF)
      }
    }
}

object HttpDirectives extends HttpDirectives {
  private val _localDateFromStringUnmarshaller = Unmarshaller.strict[String, LocalDate](TimeUtils.toLocalDate)

  private val _localTimeFromStringUnmarshaller = Unmarshaller.strict[String, LocalTime](TimeUtils.toLocalTime)

  private val _localDateTimeFromStringUnmarshaller =
    Unmarshaller.strict[String, LocalDateTime](TimeUtils.toLocalDateTime)

  private val _offsetDateTimeFromStringUnmarshaller =
    Unmarshaller.strict[String, OffsetDateTime](TimeUtils.toOffsetDateTime)
//  def ObjectIdPath: PathMatcher1[ObjectId] =
//    PathMatcher("""[\da-fA-F]{24}""".r) flatMap { string =>
//      try ObjectId.parse(string).toOption
//      catch {
//        case _: IllegalArgumentException => None
//      }
//    }

//  def ObjectIdSegment: PathMatcher1[String] =
//    PathMatcher("""[\da-fA-F]{24}""".r) flatMap { string =>
//      Some(string).filter(ObjectId.isValid)
//    }
}
