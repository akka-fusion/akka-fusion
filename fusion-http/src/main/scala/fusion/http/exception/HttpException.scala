package fusion.http.exception

import helloscala.common.ErrCodes
import helloscala.common.exception.HSException

case class HttpException(
    override val httpStatus: Int,
    message: String,
    cause: Throwable = null
) extends HSException(ErrCodes.INTERNAL_ERROR, message, cause) {}
