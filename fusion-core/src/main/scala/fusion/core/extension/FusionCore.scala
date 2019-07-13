package fusion.core.extension

import java.nio.file.Paths

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.ConfigKeys
import fusion.common.constant.FusionConstants
import fusion.core.setting.CoreSetting
import fusion.core.util.FusionUtils
import helloscala.common.Configuration
import helloscala.common.util.PidFile
import helloscala.common.util.Utils

import scala.util.control.NonFatal

final class FusionCore private (protected val _system: ExtendedActorSystem) extends FusionExtension with StrictLogging {
  FusionUtils.setupActorSystem(system)
  writePidfile()
  System.setProperty(
    FusionConstants.NAME_PATH,
    if (system.settings.config.hasPath(FusionConstants.NAME_PATH))
      system.settings.config.getString(FusionConstants.NAME_PATH)
    else FusionConstants.NAME)

  private lazy val _configuration = Configuration(system.settings.config)

  def configuration(): Configuration = _configuration
  val name: String                   = system.name
  val setting: CoreSetting           = new CoreSetting(configuration())

  private def writePidfile(): Unit = {
    val config = system.settings.config
    val maybePidfile =
      if (config.hasPath(ConfigKeys.FUSION.PIDFILE)) Utils.option(config.getString(ConfigKeys.FUSION.PIDFILE)) else None

    maybePidfile match {
      case Some(pidfile) =>
        try {
          PidFile(Utils.getPid).create(Paths.get(pidfile), deleteOnExit = true)
        } catch {
          case NonFatal(e) =>
            logger.error(s"将进程ID写入文件：$pidfile 失败", e)
            System.exit(-1)
        }
      case _ =>
        logger.warn(s"-D${ConfigKeys.FUSION.PIDFILE} 未设置，将不写入 .pid 文件。")
    }

  }

}

object FusionCore extends ExtensionId[FusionCore] with ExtensionIdProvider {
  override def lookup(): ExtensionId[_ <: Extension]                    = FusionCore
  override def createExtension(system: ExtendedActorSystem): FusionCore = new FusionCore(system)
}
