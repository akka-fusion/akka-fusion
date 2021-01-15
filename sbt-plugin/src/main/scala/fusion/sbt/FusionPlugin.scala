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

package fusion.sbt

import sbt.AutoPlugin

object Fusion extends AutoPlugin {
  val autoImport = FusionImport
}

object FusionPlugin extends AutoPlugin {
  override def requires = Fusion

  trait Keys { _: autoImport.type =>
  }

  object autoImport extends Keys
  import autoImport._

  override def projectSettings: Seq[sbt.Setting[_]] =
    defaultSettings //++ configSettings(Compile) ++ configSettings(Test)

  private def defaultSettings =
    Seq()
}
