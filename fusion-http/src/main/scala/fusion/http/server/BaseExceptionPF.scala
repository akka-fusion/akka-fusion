package fusion.http.server
import java.util.concurrent.TimeoutException

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.extractUri
import akka.http.scaladsl.server.ExceptionHandler
import com.typesafe.scalalogging.StrictLogging
import fusion.http.exception.HttpException

/**
 * 基本异常处理函数
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-01.
 */
trait BaseExceptionPF extends StrictLogging {

  def exceptionHandlerPF: ExceptionHandler.PF = {
    case ex: Throwable =>
      extractUri { uri =>
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

          case _ =>
            logger.error(s"请求无法正常处理，URI[$uri]", ex)
            jsonEntity(StatusCodes.InternalServerError, "请求无法正常处理")
        }
        complete(response)
      }
  }

  final val exceptionHandler = ExceptionHandler(exceptionHandlerPF)

}
