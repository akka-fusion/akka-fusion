package fusion.slick

import akka.actor.typed.{ActorSystem, Extension, ExtensionId}
import fusion.jdbc.FusionJdbc
import slick.jdbc.JdbcBackend
import slick.util.AsyncExecutor

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date   2021-04-26 16:19:21
 */
class SlickExtension()(implicit system: ActorSystem[_]) extends Extension {
  val db = JdbcBackend.Database.forDataSource(FusionJdbc(system).component, Some(2), AsyncExecutor("msg", 2, 2, 100))
  system.classicSystem.registerOnTermination(() => db.close())
}

object SlickExtension extends ExtensionId[SlickExtension] {
  override def createExtension(system: ActorSystem[_]): SlickExtension = new SlickExtension()(system)
}

