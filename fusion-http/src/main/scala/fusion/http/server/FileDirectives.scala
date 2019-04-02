package fusion.http.server

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fusion.http.model.FileTemp
import helloscala.common.util.DigestUtils
import helloscala.common.util.StringUtils

import scala.collection.immutable
import scala.concurrent.Future

trait FileDirectives {

  def uploadedMultiFile(tmpDirectory: Path): Directive1[immutable.Seq[(FileInfo, Path)]] =
    entity(as[Multipart.FormData])
      .flatMap { formData ⇒
        extractRequestContext.flatMap { ctx ⇒
          import ctx.executionContext
          import ctx.materializer

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

  def uploadedOneFile: Directive1[(FileInfo, Source[ByteString, Any])] = entity(as[Multipart.FormData]).flatMap {
    formData ⇒
      Directive[Tuple1[(FileInfo, Source[ByteString, Any])]] { inner ⇒ ctx ⇒
        import ctx.executionContext
        import ctx.materializer

        // Streamed multipart data must be processed in a certain way, that is, before you can expect the next part you
        // must have fully read the entity of the current part.
        // That means, we cannot just do `formData.parts.runWith(Sink.seq)` and then look for the part we are interested in
        // but instead, we must actively process all the parts, regardless of whether we are interested in the data or not.
        // Fortunately, continuation passing style of routing allows adding pre- and post-processing quite naturally.
        formData.parts
          .runFoldAsync(Option.empty[RouteResult]) {
            case (None, part) if part.filename.isDefined ⇒
              val data = (FileInfo(part.name, part.filename.get, part.entity.contentType), part.entity.dataBytes)
              inner(Tuple1(data))(ctx).map(Some(_))

            case (res, part) ⇒
              part.entity.discardBytes()
              Future.successful(res)
          }
          .map(_.getOrElse(RouteResult.Rejected(ValidationRejection("filename未指定") :: Nil)))
      }
  }

  def uploadedShaFile(tmpDirectory: Path): Directive[(FileInfo, FileTemp)] =
    extractRequestContext.flatMap { ctx ⇒
      import ctx.executionContext
      import ctx.materializer
      uploadedOneFile.flatMap {
        case (fileInfo, source) =>
          val sha     = DigestUtils.digestSha256()
          val tmpPath = Files.createTempFile(tmpDirectory, "", "upload")
          val uploadF = source
            .map { bytes =>
              sha.update(bytes.asByteBuffer)
              bytes
            }
            .runWith(FileIO.toPath(tmpPath))
            .map {
              case result if result.wasSuccessful =>
                val hash = StringUtils.toHexString(sha.digest())
                (fileInfo, FileTemp(hash, result.count, tmpPath))
              case _ =>
                throw new IOException("文件写入失败")
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
      import ctx.executionContext
      import ctx.materializer
      uploadedMultiFile(tmpDirectory).flatMap { list =>
        val futures = list.map {
          case (fileInfo, path) =>
            DigestUtils.reactiveSha256Hex(path).map(hash => fileInfo -> FileTemp(hash, Files.size(path), path))
        }
        onSuccess(Future.sequence(futures))
      }
    }

}

object FileDirectives extends FileDirectives {}
