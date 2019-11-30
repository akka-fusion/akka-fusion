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

import helloscala.common.util.{ EnumTrait, EnumTraitCompanion }

sealed trait ExecuteMode extends EnumTrait

object ExecuteMode extends EnumTraitCompanion {
  case object UNKNOWN extends ExecuteMode {
    override def companion: ExecuteMode.type = ExecuteMode
  }
  case object STANDALONE extends ExecuteMode {
    override def companion: ExecuteMode.type = ExecuteMode
  }
  case object BROADCAST extends ExecuteMode {
    override def companion: ExecuteMode.type = ExecuteMode
  }
  case object PARALLEL extends ExecuteMode {
    override def companion: ExecuteMode.type = ExecuteMode
  }
  case object GRID extends ExecuteMode {
    override def companion: ExecuteMode.type = ExecuteMode
  }
  case object BATCH extends ExecuteMode {
    override def companion: ExecuteMode.type = ExecuteMode
  }
}
