package fusion.job

import akka.actor.CoordinatedShutdown
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.extension.FusionExtension

class FusionJob private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new FusionJobComponents(_system)
  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "FusionJob") { () =>
    components.closeAsync()(system.dispatcher)
  }
  def component: FusionScheduler = components.component
}

object FusionJob extends ExtensionId[FusionJob] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionJob = new FusionJob(system)
  override def lookup(): ExtensionId[_ <: Extension]                   = FusionJob
}
