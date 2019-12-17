/*
 * Copyright 2019 akka-fusion.com
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

package fusion.discovery.model

import fusion.json.JsonUtils
import fusion.test.FusionTestWordSpec
import helloscala.common.IntStatus
import org.scalatest.Matchers
import scalapb.GeneratedMessage

class ModelSpec extends FusionTestWordSpec with Matchers {
  "Instance models" should {
    "InstanceReply" in {
      dumpJsonString(InstanceReply())
      dumpJsonString(InstanceReply(IntStatus.BAD_REQUEST))
      dumpJsonString(
        InstanceReply(IntStatus.BAD_REQUEST, InstanceReply.Data.Queried(InstanceQueried(List(Instance())))))
//      dumpJsonString(InstanceReply(IntStatus.BAD_REQUEST, InstanceReply.Data.Modified(InstanceModified(404))))
    }
  }

  private def dumpJsonString(msg: GeneratedMessage): Unit = {
    println(JsonUtils.protobuf.toJsonString(msg))
  }
}
