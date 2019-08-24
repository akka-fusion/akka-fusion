package fusion.core.setting

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import helloscala.common.Configuration

import scala.concurrent.duration._

final class CircuitBreakerSetting private (c: Configuration) {
  def enable: Boolean              = c.getOrElse("circuit.enable", true)
  def maxFailures: Int             = c.getOrElse("circuit.max-failures", 5)
  def callTimeout: FiniteDuration  = c.getOrElse("circuit.call-timeout", 30.seconds)
  def resetTimeout: FiniteDuration = c.getOrElse("circuit.reset-timeout", 30.seconds)
}

object CircuitBreakerSetting {

  def apply(c: Configuration): CircuitBreakerSetting = new CircuitBreakerSetting(c)

  def apply(configuration: Configuration, prefix: String): CircuitBreakerSetting =
    apply(configuration.getConfiguration(prefix))

  def getCircuitBreaker(system: ActorSystem, prefix: String): Option[CircuitBreaker] = {
    val configuration          = Configuration(system.settings.config)
    val deftCircuitBreakerConf = configuration.getConfiguration("fusion.default.circuit-breaker")
    val c                      = configuration.getConfiguration(prefix)
    if (c.getOrElse("circuit-breaker.enable", false)) {
      val s = CircuitBreakerSetting(c.getConfiguration("circuit-breaker").withFallback(deftCircuitBreakerConf))
      Some(CircuitBreaker(system.scheduler, s.maxFailures, s.callTimeout, s.resetTimeout))
    } else {
      None
    }
  }

}
