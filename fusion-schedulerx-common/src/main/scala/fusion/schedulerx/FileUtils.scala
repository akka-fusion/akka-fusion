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

package fusion.schedulerx

import java.io.IOException
import java.nio.file.{ Files, Path, Paths }
import java.security.AccessController.doPrivileged
import java.time.LocalDate

import sun.security.action.GetPropertyAction

object FileUtils {
  val systemTmpDir: Path = Paths.get(doPrivileged(new GetPropertyAction("java.io.tmpdir")))
  def createWorkerRunDirectory(jobId: String): Path = {
    val dir = systemTmpDir.resolve(s"${LocalDate.now()}/$jobId")
    if (Files.isRegularFile(dir)) {
      throw new IOException(s"路径已存在，且不是目录！path: $dir")
    } else if (!Files.exists(dir)) {
      Files.createDirectories(dir)
    }
    dir
  }
}
