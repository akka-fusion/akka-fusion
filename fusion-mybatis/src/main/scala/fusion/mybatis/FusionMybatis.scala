package fusion.mybatis

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.extension.FusionExtension

class FusionMybatis private (override protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components: MybatisComponents      = new MybatisComponents(_system)
  def component: FusionSqlSessionFactory = components.component
}

object FusionMybatis extends ExtensionId[FusionMybatis] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMybatis = new FusionMybatis(system)
  override def lookup(): ExtensionId[_ <: Extension]                       = FusionMybatis
}
