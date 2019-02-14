package fusion.http.exception

import akka.http.scaladsl.model.StatusCode
import helloscala.common.exception.HSException

case class HttpException(
    httpStatus: StatusCode,
    override val message: String,
    override val cause: Throwable = null
) extends HSException(message, cause)
