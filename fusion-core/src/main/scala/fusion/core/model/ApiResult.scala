/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.core.model

import helloscala.common.IntStatus

case class ApiResult(status: Int, msg: String, data: Any)

object ApiResult {
  def apply(status: Int, message: String): ApiResult = ApiResult(status, message, null)
  def success() = ApiResult(IntStatus.SUCCESS, "", null)
  def success(data: Any) = ApiResult(IntStatus.SUCCESS, "", data)
  def ok() = ApiResult(IntStatus.OK, "", null)
  def ok(data: Any) = ApiResult(IntStatus.OK, "", data)
}
