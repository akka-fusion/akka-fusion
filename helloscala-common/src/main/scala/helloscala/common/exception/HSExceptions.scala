package helloscala.common.exception

import helloscala.common.IntStatus

case class HSAcceptedWarning(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.ACCEPTED,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.ACCEPTED
}

case class HSBadRequestException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.BAD_REQUEST,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.BAD_REQUEST
}

case class HSUnauthorizedException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.UNAUTHORIZED,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.UNAUTHORIZED
}

case class HSNoContentException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.NO_CONTENT,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.NO_CONTENT
}

case class HSForbiddenException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.FORBIDDEN,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.FORBIDDEN
}

case class HSNotFoundException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.NOT_FOUND,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.NOT_FOUND
}

case class HSConfigurationException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.NOT_FOUND_CONFIG,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.NOT_FOUND
}

case class HSConflictException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.CONFLICT,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.CONFLICT
}

case class HSNotImplementedException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.NOT_IMPLEMENTED,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.NOT_IMPLEMENTED
}

case class HSInternalErrorException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.INTERNAL_ERROR,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.INTERNAL_ERROR
}

case class HSBadGatewayException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.BAD_GATEWAY,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.BAD_GATEWAY
}

case class HSServiceUnavailableException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.SERVICE_UNAVAILABLE,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.SERVICE_UNAVAILABLE
}

case class HSGatewayTimeoutException(
    override val msg: String,
    override val data: AnyRef = null,
    override val status: Int = IntStatus.GATEWAY_TIMEOUT,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = IntStatus.GATEWAY_TIMEOUT
}
