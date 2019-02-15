package fusion.core.inject.builtin

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.AbstractModule
import helloscala.common.Configuration
import javax.inject.{Inject, Provider, Singleton}

@Singleton
class ConfigurationProvider @Inject()() extends Provider[Configuration] {
  private[this] val configuration = Configuration()

  override def get(): Configuration = configuration
}

@Singleton
class ActorSystemProvider @Inject()(configuration: Configuration) extends Provider[ActorSystem] {
  private[this] val system = ActorSystem(configuration.getString("fusion.name"), configuration.underlying)
  sys.addShutdownHook { system.terminate() }

  override def get(): ActorSystem = system
}

@Singleton
class ActorMaterializerProvider @Inject()(system: ActorSystem) extends Provider[ActorMaterializer] {
  private[this] val materializer = ActorMaterializer()(system)

  override def get(): ActorMaterializer = materializer
}

class BuiltinModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Configuration]).toProvider(classOf[ConfigurationProvider])
    bind(classOf[ActorSystem]).toProvider(classOf[ActorSystemProvider])
    bind(classOf[ActorMaterializer]).toProvider(classOf[ActorMaterializerProvider])
  }
}
