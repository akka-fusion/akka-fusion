package fusion.core.model

import helloscala.common.ErrCodes

case class ApiResult(status: Int, message: String, data: Any)

object ApiResult {
  def success() = ApiResult(ErrCodes.SUCCESS, "", null)

  def success(data: Any) = ApiResult(ErrCodes.SUCCESS, "", data)
}
