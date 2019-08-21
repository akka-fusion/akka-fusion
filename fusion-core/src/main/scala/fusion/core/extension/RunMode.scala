package fusion.core.extension

import helloscala.common.Configuration

class RunMode(configuration: Configuration) {

  val active: String = configuration
    .get[Option[String]]("fusion.profiles.active")
    .orElse(configuration.get[Option[String]]("spring.profiles.active"))
    .getOrElse("dev")

  def isDev: Boolean  = active == "dev"
  def isProd: Boolean = active == "prod"
  def isTest: Boolean = active == "test"
  def isIt: Boolean   = active == "it" || isTest
}
