package fusion.core.model

import helloscala.common.IntStatus

case class ApiResult(status: Int, msg: String, data: Any)

object ApiResult {
  def apply(status: Int, message: String): ApiResult = ApiResult(status, message, null)
  def success()                                      = ApiResult(IntStatus.SUCCESS, "", null)
  def success(data: Any)                             = ApiResult(IntStatus.SUCCESS, "", data)
  def ok()                                           = ApiResult(IntStatus.OK, "", null)
  def ok(data: Any)                                  = ApiResult(IntStatus.OK, "", data)
}
