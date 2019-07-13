package fusion.http.exception

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCode
import helloscala.common.IntStatus
import helloscala.common.exception.HSException

case class HttpException(
    statusCode: StatusCode,
    override val msg: String,
    override val data: Object = null,
    override val status: Int = IntStatus.INTERNAL_ERROR,
    override val cause: Throwable = null)
    extends HSException(status, msg, cause) {
  override val httpStatus: Int = statusCode.intValue()
}

case class HttpResponseException(httpResponse: HttpResponse) extends HSException(IntStatus.OK)
