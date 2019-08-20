package fusion.http.util

import java.util.concurrent.TimeoutException

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.StatusCodes.RequestEntityTooLarge
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.RequestContext
import com.typesafe.scalalogging.StrictLogging
import fusion.http.exception.HttpResponseException
import helloscala.common.exception.HSException

object BaseExceptionHandler extends StrictLogging {
  import HttpUtils._

  def exceptionHandlerPF: ExceptionHandler.PF = {
    case HttpResponseException(response) =>
      complete(response)

    case ex: Throwable =>
      ctx: RequestContext =>
        val uri = ctx.request.uri
        ctx.request.discardEntityBytes(ctx.materializer)
        val response = ex match {
          case e: HSException =>
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
