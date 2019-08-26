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

package fusion.core.http.headers

import java.time.Instant

import akka.http.scaladsl.model.headers.ModeledCustomHeader
import akka.http.scaladsl.model.headers.ModeledCustomHeaderCompanion
import fusion.common.constant.FusionConstants

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

final class `X-Service`(override val value: String) extends ModeledCustomHeader[`X-Service`] {
  override def companion: ModeledCustomHeaderCompanion[`X-Service`] = `X-Service`
  override def renderInRequests(): Boolean = true
  override def renderInResponses(): Boolean = true
}

object `X-Service` extends ModeledCustomHeaderCompanion[`X-Service`] {
  override def name: String = FusionConstants.X_SERVER
  override def parse(value: String): Try[`X-Service`] = Try(new `X-Service`(value))
}

final class `X-Trace-Id`(override val value: String) extends ModeledCustomHeader[`X-Trace-Id`] {
  override def companion: ModeledCustomHeaderCompanion[`X-Trace-Id`] = `X-Trace-Id`
  override def renderInRequests(): Boolean = true
  override def renderInResponses(): Boolean = true
}

object `X-Trace-Id` extends ModeledCustomHeaderCompanion[`X-Trace-Id`] {
  override def name: String = FusionConstants.X_TRACE_NAME
  override def parse(value: String): Try[`X-Trace-Id`] = Try(new `X-Trace-Id`(value))
}

final class `X-Request-Time`(val instant: Instant) extends ModeledCustomHeader[`X-Request-Time`] {
  override def value(): String = instant.toString
  override def companion: ModeledCustomHeaderCompanion[`X-Request-Time`] = `X-Request-Time`
  override def renderInRequests(): Boolean = true
  override def renderInResponses(): Boolean = true
}

object `X-Request-Time` extends ModeledCustomHeaderCompanion[`X-Request-Time`] {
  override def name: String = FusionConstants.X_REQUEST_TIME
  override def parse(value: String): Try[`X-Request-Time`] = Try(new `X-Request-Time`(Instant.parse(value)))
  def fromInstantNow() = `X-Request-Time`(Instant.now().toString)
}

final class `X-Span-Time`(val duration: java.time.Duration) extends ModeledCustomHeader[`X-Span-Time`] {
  override def value(): String = duration.toString
  override def companion: ModeledCustomHeaderCompanion[`X-Span-Time`] = `X-Span-Time`
  override def renderInRequests(): Boolean = true
  override def renderInResponses(): Boolean = true
}

object `X-Span-Time` extends ModeledCustomHeaderCompanion[`X-Span-Time`] {
  override def name: String = FusionConstants.X_SPAN_TIME
  override def parse(value: String): Try[`X-Span-Time`] = Try(new `X-Span-Time`(java.time.Duration.parse(value)))

  def apply(d: FiniteDuration): `X-Span-Time` = {
    import scala.compat.java8.DurationConverters._
    new `X-Span-Time`(d.toJava)
  }

  def fromXRequestTime(h: `X-Request-Time`) =
    `X-Span-Time`(java.time.Duration.between(Instant.parse(h.value), Instant.now()).toString)
}
