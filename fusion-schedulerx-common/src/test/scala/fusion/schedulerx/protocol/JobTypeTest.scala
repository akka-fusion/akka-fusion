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

package fusion.schedulerx.protocol

import org.scalatest.{ FunSuite, Matchers }

class JobTypeTest extends FunSuite with Matchers {
  test("JobType") {
    JobType.JAVA.id should be(0)
    JobType.JAVA.name should be("java")
    JobType.SHELL.id should be(1)
    JobType.SHELL.name should be("shell")
    JobType.PYTHON.id should be(2)
    JobType.PYTHON.name should be("python")
    JobType.GO.id should be(3)
    JobType.GO.name should be("go")
  }
}
