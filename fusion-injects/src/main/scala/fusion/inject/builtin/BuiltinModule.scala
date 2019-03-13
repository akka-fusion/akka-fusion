package fusion.inject.builtin

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.AbstractModule
import com.typesafe.config.Config
import helloscala.common.Configuration
import javax.inject.{Inject, Provider, Singleton}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

@Singleton
class ConfigurationProvider @Inject()() extends Provider[Configuration] {
  private[this] val configuration = Configuration()

  override def get(): Configuration = configuration
}

@Singleton
class ConfigProvider @Inject()(configuration: Configuration) extends Provider[Config] {
  override def get(): Config = configuration.underlying
}

@Singleton
class ActorSystemProvider @Inject()(configuration: Configuration) extends Provider[ActorSystem] {
  private[this] val system = ActorSystem(configuration.getString("fusion.name"), configuration.underlying)
  sys.addShutdownHook { system.terminate() }

  override def get(): ActorSystem = system
}

@Singleton
class ExecutionContextExecutorProvider @Inject()(system: ActorSystem) extends Provider[ExecutionContextExecutor] {
  override def get(): ExecutionContextExecutor = system.dispatcher
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
    bind(classOf[ExecutionContextExecutor]).toProvider(classOf[ExecutionContextExecutorProvider])
    bind(classOf[ExecutionContext]).to(classOf[ExecutionContextExecutor])
    bind(classOf[ActorMaterializer]).toProvider(classOf[ActorMaterializerProvider])
  }
}
