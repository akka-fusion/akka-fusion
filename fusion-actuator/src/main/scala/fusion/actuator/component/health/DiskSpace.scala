package fusion.actuator.component.health

import java.nio.file.FileSystems

import fusion.core.model.{Health, HealthComponent}

object DiskSpace extends HealthComponent {
  override def health: Health = {
    var free  = 0L
    var total = 0L
    try {
      val fs = FileSystems.getDefault
      fs.getFileStores.forEach { store =>
        total += store.getTotalSpace
        free += store.getUsableSpace
      }
      Health.up("total" -> total, "free" -> free)
    } catch {
      case _: Throwable =>
        Health.down("total" -> total, "free" -> free)
    }
  }
}
