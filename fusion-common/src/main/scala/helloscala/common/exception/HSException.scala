package helloscala.common.exception

class HSException(val message: String, val cause: Throwable) extends RuntimeException(message, cause) {
  def this(message: String) {
    this(message, null)
  }
  def this() {
    this(null, null)
  }
}
