package helloscala.common.exception

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import helloscala.common.ErrCodes

@JsonIgnoreProperties(value = Array("suppressed", "localizedMessage", "message", "stackTrace", "cause"))
class HSException(
    errCode: Int,
    message: String,
    cause: Throwable,
    enableSuppression: Boolean,
    writableStackTrace: Boolean
) extends RuntimeException(message, cause, enableSuppression, writableStackTrace) {

  val httpStatus: Int = ErrCodes.INTERNAL_ERROR

  val data: Object = null

  def this(errCode: Int) {
    this(errCode, null, null, true, false)
  }

  def this(errCode: Int, message: String) {
    this(errCode, message, null, true, false)
  }

  def this(errCode: Int, message: String, cause: Throwable) {
    this(errCode, message, cause, true, false)
  }

  def this(message: String) {
    this(ErrCodes.INTERNAL_ERROR, message, null, true, false)
  }

  def this(message: String, cause: Throwable) {
    this(ErrCodes.INTERNAL_ERROR, message, cause, true, false)
  }

  def getErrCode: Int = errCode
}
