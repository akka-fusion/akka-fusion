package fusion.http.exception

import helloscala.common.ErrCodes
import helloscala.common.exception.HSException

case class HttpException(
    override val httpStatus: Int,
    message: String,
    override val data: Object = null,
    errCode: Int = ErrCodes.INTERNAL_ERROR,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {}
