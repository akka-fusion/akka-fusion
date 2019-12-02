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

sealed trait JobType extends EnumTrait {
  def program: String
  def ext: String
}

object JobType extends EnumTraitCompanion {
  case object UNKNOWN extends JobType {
    override def program: String = ""

    override def ext: String = ""

    override def companion: JobType.type = JobType
  }
  case object JAVA extends JobType {
    override def ext: String = "java"

    override def program: String = "java"

    override def companion: JobType.type = JobType
  }
  case object SHELL extends JobType {
    override def ext: String = "sh"

    override def program: String = "sh"

    override def companion: JobType.type = JobType
  }
  case object PYTHON extends JobType {
    override def ext: String = "py"

    override def program: String = "python"

    override def companion: JobType.type = JobType
  }
  case object GO extends JobType {
    override def ext: String = "go"

    override def program: String = "go"

    override def companion: JobType.type = JobType
  }
}
