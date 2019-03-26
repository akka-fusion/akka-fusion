package fusion.inject.builtin

import helloscala.common.Configuration
import javax.inject.{Inject, Provider, Singleton}

@Singleton
class ConfigurationProvider @Inject()() extends Provider[Configuration] {
  private[this] val configuration   = Configuration.fromDiscovery()
  override def get(): Configuration = configuration
}
