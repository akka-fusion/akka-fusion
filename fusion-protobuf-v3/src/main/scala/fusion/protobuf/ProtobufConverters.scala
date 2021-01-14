/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.protobuf

import java.time.{Instant, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import com.google.protobuf.duration.Duration
import com.google.protobuf.timestamp.Timestamp

import scala.concurrent.duration.FiniteDuration

trait ProtobufConverters {

  implicit class ProtobufTimestampConverter(value: Timestamp) {
    @inline def toInstant: Instant = toJavaInstant

    def toJavaInstant: Instant = {
      Instant.ofEpochSecond(value.seconds, value.nanos)
    }
  }

  implicit class ProtobufDurationConverter(value: Duration) {
    def toJava: java.time.Duration = java.time.Duration.ofSeconds(value.seconds, value.nanos)
    def toScala = FiniteDuration(toJava.toNanos, TimeUnit.NANOSECONDS)
  }

  implicit class JavaDurationToProtobuf(value: java.time.Duration) {
    def toProtobuf: Duration = Duration(value.getSeconds, value.getNano)
  }

  implicit class ScalaDurationToProtobuf(value: scala.concurrent.duration.Duration) {

    def toProtobuf: Duration = {
      val seconds = value.toSeconds
      val nanos = (value.toNanos - (seconds * 1000000000L)).toInt
      Duration(seconds, nanos)
    }
  }

  implicit class InstantToTimestamp(inst: Instant) {
    def toProtobuf: Timestamp = Timestamp(inst.getEpochSecond, inst.getNano)
  }

  implicit class LocalDateTimeToTimestamp(ldt: LocalDateTime) {
    def toProtobuf: Timestamp = ldt.toInstant(ZoneOffset.from(OffsetDateTime.now())).toProtobuf
  }
}

object ProtobufConverters extends ProtobufConverters
