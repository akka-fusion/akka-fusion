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

package fusion.schedulerx.job

/**
 *
 * @param status 复用HTTP状态码，返回200代码处理成功
 * @param result 处理结果
 */
final class ProcessResult(val status: Int, val result: String) {
  def this(result: String) {
    this(200, result)
  }
}

object ProcessResult {
  val Empty = apply(-1, "")
  def apply(status: Int, result: String): ProcessResult = new ProcessResult(status, result)
  def ok(result: String): ProcessResult = new ProcessResult(result)
}
