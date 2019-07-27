package helloscala.common.exception

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import helloscala.common.IntStatus

@JsonIgnoreProperties(value = Array("suppressed", "localizedMessage", "stackTrace", "cause"))
class HSException(
    val status: Int,
    val msg: String,
    val cause: Throwable,
    enableSuppression: Boolean,
    writableStackTrace: Boolean)
    extends RuntimeException(msg, cause, enableSuppression, writableStackTrace) {

  def httpStatus: Int = IntStatus.INTERNAL_ERROR

  val data: Object = null

  def this(status: Int) {
    this(status, null, null, true, false)
  }

  def this(status: Int, message: String) {
    this(status, message, null, true, false)
  }

  def this(status: Int, message: String, cause: Throwable) {
    this(status, message, cause, true, false)
  }

  def this(message: String) {
    this(IntStatus.INTERNAL_ERROR, message, null, true, false)
  }

  def this(message: String, cause: Throwable) {
    this(IntStatus.INTERNAL_ERROR, message, cause, true, false)
  }

  def getStatus(): Int = status
}
