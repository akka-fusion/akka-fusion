/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.core.setting

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.typesafe.config.ConfigFactory
import helloscala.common.Configuration

import scala.concurrent.duration._

final class CircuitBreakerSetting private (c: Configuration) {
  def enable: Boolean = c.getOrElse("enable", false)
  def maxFailures: Int = c.getOrElse("max-failures", 5)
  def callTimeout: FiniteDuration = c.getOrElse("call-timeout", 30.seconds)
  def resetTimeout: FiniteDuration = c.getOrElse("reset-timeout", 30.seconds)
}

object CircuitBreakerSetting {

  def apply(c: Configuration): CircuitBreakerSetting = new CircuitBreakerSetting(c)

  def apply(configuration: Configuration, prefix: String): CircuitBreakerSetting =
    apply(configuration.getOrElse(prefix, Configuration(ConfigFactory.parseString("{}"))))

  def getCircuitBreaker(system: ActorSystem, prefix: String): Option[CircuitBreaker] = {
    val configuration = Configuration(system.settings.config)
    val deftCircuitBreakerConf = configuration.getConfiguration("fusion.default.circuit-breaker")
    val c = configuration.getOrElse(prefix, Configuration(ConfigFactory.parseString("{}")))
    if (c.getOrElse("enable", false)) {
      val s = CircuitBreakerSetting(c.withFallback(deftCircuitBreakerConf))
      Some(CircuitBreaker(system.scheduler, s.maxFailures, s.callTimeout, s.resetTimeout))
    } else {
      None
    }
  }

}
