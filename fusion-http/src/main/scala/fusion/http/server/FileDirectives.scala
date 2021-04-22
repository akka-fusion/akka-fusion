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

import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.{ FileIO, Sink, Source }
import akka.util.ByteString
import fusion.common.util.StreamUtils
import fusion.http.model.FileTemp
import helloscala.common.util.{ DigestUtils, StringUtils }

import java.io.{ File, IOException }
import java.nio.file.{ Files, Path, Paths }
import scala.collection.immutable
import scala.concurrent.Future

trait FileDirectives {

  def createTempFileFunc(
      dir: java.nio.file.Path = Paths.get("/tmp"),
      prefix: String = "fusion-",
      suffix: String = ".tmp"): FileInfo => File =
    fileInfo => Files.createTempFile(dir, fileInfo.fileName, suffix).toFile

  def uploadedMultiFile(tmpDirectory: Path): Directive1[immutable.Seq[(FileInfo, Path)]] =
    entity(as[Multipart.FormData])
      .flatMap { formData =>
        extractRequestContext.flatMap { ctx =>
          import ctx.{ executionContext, materializer }

          val multiPartF = formData.parts
            .map { part =>
              val destination = Files.createTempFile(tmpDirectory, part.name, ".tmp")
              val uploadedF: Future[(FileInfo, Path)] =
                part.entity.dataBytes
                  .runWith(FileIO.toPath(destination))
                  .map(_ => (FileInfo(part.name, part.filename.get, part.entity.contentType), destination))
              uploadedF
            }
            .runWith(Sink.seq)
            .flatMap(list => Future.sequence(list))

          onSuccess(multiPartF)
        }
      }
      .flatMap {
        case Nil  => reject(ValidationRejection("没有任何上传文件"))
        case list => provide(list)
      }

  def uploadedOneFile: Directive1[(FileInfo, Source[ByteString, Any])] =
    entity(as[Multipart.FormData]).flatMap { formData =>
      Directive[Tuple1[(FileInfo, Source[ByteString, Any])]] { inner => ctx =>
        import ctx.{ executionContext, materializer }

        // Streamed multipart data must be processed in a certain way, that is, before you can expect the next part you
        // must have fully read the entity of the current part.
        // That means, we cannot just do `formData.parts.runWith(Sink.seq)` and then look for the part we are interested in
        // but instead, we must actively process all the parts, regardless of whether we are interested in the data or not.
        // Fortunately, continuation passing style of routing allows adding pre- and post-processing quite naturally.
        formData.parts
          .runFoldAsync(Option.empty[RouteResult]) {
            case (None, part) if part.filename.isDefined =>
              val data = (FileInfo(part.name, part.filename.get, part.entity.contentType), part.entity.dataBytes)
              inner(Tuple1(data))(ctx).map(Some(_))

            case (res, part) =>
              part.entity.discardBytes()
              Future.successful(res)
          }
          .map(_.getOrElse(RouteResult.Rejected(ValidationRejection("filename未指定") :: Nil)))
      }
    }

  def uploadedShaFile(tmpDirectory: Path): Directive[(FileInfo, FileTemp)] =
    extractRequestContext.flatMap { ctx =>
      import ctx.{ executionContext, materializer }
      uploadedOneFile.flatMap {
        case (fileInfo, source) =>
          val sha = DigestUtils.digestSha256()
          val tmpPath = Files.createTempFile(tmpDirectory, "", "upload")
          val uploadF = source
            .map { bytes =>
              sha.update(bytes.asByteBuffer)
              bytes
            }
            .runWith(FileIO.toPath(tmpPath))
            .map {
              case result if result.count > 0L =>
                val hash = StringUtils.toHexString(sha.digest())
                (fileInfo, FileTemp(hash, result.count, tmpPath))
              case _ =>
                throw new IOException(s"未写入任何数据到文件：$tmpPath")
            }
            .recoverWith {
              case e =>
                Files.deleteIfExists(tmpPath)
                throw e
            }
          onSuccess(uploadF)
      }
    }

  def uploadedMultiShaFile(tmpDirectory: Path): Directive1[immutable.Seq[(FileInfo, FileTemp)]] =
    extractRequestContext.flatMap { ctx =>
      import ctx.{ executionContext, materializer }
      uploadedMultiFile(tmpDirectory).flatMap { list =>
        val futures = list.map {
          case (fileInfo, path) =>
            StreamUtils.reactiveSha256Hex(path).map(hash => fileInfo -> FileTemp(hash, Files.size(path), path))
        }
        val seqF = Future.sequence(futures)
        onSuccess(seqF)
      }
    }
}

object FileDirectives extends FileDirectives {}
