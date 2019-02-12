package fusion.http.exception

import akka.http.scaladsl.model.StatusCodes
import helloscala.exception.HSException

class HttpException(
    override val message: String,
    override val cause: Throwable = null
) extends HSException(message, cause) {

  def httpStatus = StatusCodes.InternalServerError
}
