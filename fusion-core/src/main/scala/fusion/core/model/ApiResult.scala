package fusion.core.model

import helloscala.common.ErrCodes

case class ApiResult(status: Int, message: String, data: Any)

object ApiResult {
  def apply(status: Int, message: String): ApiResult = ApiResult(status, message, null)
  def success() = ApiResult(ErrCodes.SUCCESS, "", null)
  def success(data: Any) = ApiResult(ErrCodes.SUCCESS, "", data)
  def ok() = ApiResult(ErrCodes.OK, "", null)
  def ok(data: Any) = ApiResult(ErrCodes.OK, "", data)
}
