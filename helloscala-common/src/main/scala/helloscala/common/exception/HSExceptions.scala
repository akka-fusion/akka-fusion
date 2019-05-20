package helloscala.common.exception

import helloscala.common.ErrCodes

case class HSAcceptedWarning(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.ACCEPTED,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.ACCEPTED
}

case class HSBadRequestException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.BAD_REQUEST,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.BAD_REQUEST
}

case class HSUnauthorizedException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.UNAUTHORIZED,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.UNAUTHORIZED
}

case class HSNoContentException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.NO_CONTENT,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.NO_CONTENT
}

case class HSForbiddenException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.FORBIDDEN,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.FORBIDDEN
}

case class HSNotFoundException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.NOT_FOUND,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.NOT_FOUND
}

case class HSConfigurationException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.NOT_FOUND_CONFIG,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.NOT_FOUND
}

case class HSConflictException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.CONFLICT,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.CONFLICT
}

case class HSNotImplementedException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.NOT_IMPLEMENTED,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.NOT_IMPLEMENTED
}

case class HSInternalErrorException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.INTERNAL_ERROR,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.INTERNAL_ERROR
}

case class HSBadGatewayException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.BAD_GATEWAY,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.BAD_GATEWAY
}

case class HSServiceUnavailableException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.SERVICE_UNAVAILABLE,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.SERVICE_UNAVAILABLE
}

case class HSGatewayTimeoutException(
    message: String,
    override val data: AnyRef = null,
    errCode: Int = ErrCodes.GATEWAY_TIMEOUT,
    cause: Throwable = null)
    extends HSException(errCode, message, cause) {
  override val httpStatus: Int = ErrCodes.GATEWAY_TIMEOUT
}
