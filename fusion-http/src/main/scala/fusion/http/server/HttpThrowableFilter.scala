package fusion.http.server

import java.util.concurrent.TimeoutException

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.StatusCodes.RequestEntityTooLarge
import akka.http.scaladsl.model.EntityStreamSizeException
import akka.http.scaladsl.model.ExceptionWithErrorInfo
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.IllegalRequestException
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging
import fusion.http.exception.HttpException
import fusion.http.exception.HttpResponseException
import fusion.http.server.HttpThrowableFilter.ThrowableFilter
import helloscala.common.exception.HSException

import scala.concurrent.Future

trait HttpThrowableFilter {
  def throwableFilter: ThrowableFilter
}

object HttpThrowableFilter extends StrictLogging {
  type ThrowableFilter = PartialFunction[Throwable, Future[HttpResponse]]

  def createExceptionHandler(throwableHandler: PartialFunction[Throwable, Future[HttpResponse]]): ExceptionHandler = {
    ExceptionHandler({
      case e: Throwable if throwableHandler.isDefinedAt(e) =>
        onSuccess(throwableHandler(e)) { response =>
          complete(response)
        }
      case e: Throwable => exceptionHandlerPF(e)
    })
  }

  def defaultThrowableFilter: PartialFunction[Throwable, Future[HttpResponse]] = {
    case HttpResponseException(response) => Future.successful(response)
    case e: HSException                  => Future.successful(jsonResponse(e.httpStatus, e.getMessage))
    case e: Throwable                    => Future.successful(jsonResponse(StatusCodes.InternalServerError, e.getLocalizedMessage))
  }

  def exceptionHandlerPF: ExceptionHandler.PF = {
    case HttpResponseException(response) =>
      complete(response)

    case ex: Throwable =>
      ctx: RequestContext =>
        val uri = ctx.request.uri
        ctx.request.discardEntityBytes(ctx.materializer)
        val response = ex match {
          case e: HttpException =>
            val msg = s"HTTP异常，URI[$uri] ${e.getLocalizedMessage}"
            val t   = e.getCause
            if (t != null) logger.warn(msg, t) else logger.warn(s"URI[$uri] ${e.toString}")
            jsonEntity(e.httpStatus, e.getLocalizedMessage)

          case e: IllegalArgumentException =>
            logger.warn(s"非法参数: ${e.getLocalizedMessage}", e)
            jsonEntity(StatusCodes.BadRequest, "非法参数")

          case e: TimeoutException =>
            logger.warn(s"请求超时: ${e.getLocalizedMessage}", e)
            jsonEntity(StatusCodes.GatewayTimeout, "请求超时")

          case e @ IllegalRequestException(info, status) =>
            val msg = info.format(ctx.settings.verboseErrorMessages)
            logger.warn(msg, e)
            jsonEntity(status, msg)

          case e: EntityStreamSizeException =>
            logger.warn("请求实体太大", e)
            jsonEntity(RequestEntityTooLarge, e.toString())

          case e: ExceptionWithErrorInfo =>
            val msg = e.info.format(ctx.settings.verboseErrorMessages)
            logger.warn(msg, e)
            jsonEntity(InternalServerError, msg)

          case _ =>
            logger.error(s"请求无法正常处理，URI[$uri]", ex)
            jsonEntity(StatusCodes.InternalServerError, "请求无法正常处理")
        }
        ctx.complete(response)
  }

}
