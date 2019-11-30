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

package fusion.actuator.component.health

import java.nio.file.FileStore
import java.nio.file.FileSystems
import java.util.function.Consumer

import fusion.core.model.Health
import fusion.core.model.HealthComponent

object DiskSpace extends HealthComponent {
  override def health: Health = {
    var free = 0L
    var total = 0L
    try {
      val fs = FileSystems.getDefault
      fs.getFileStores.forEach(new Consumer[FileStore] {
        override def accept(store: FileStore): Unit = {
          total += store.getTotalSpace
          free += store.getUsableSpace
        }
      })
      Health.up("total" -> total, "free" -> free)
    } catch {
      case _: Throwable =>
        Health.down("total" -> total, "free" -> free)
    }
  }
}
