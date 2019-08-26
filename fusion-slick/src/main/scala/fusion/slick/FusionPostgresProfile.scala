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

package fusion.slick

import java.time._

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.str.PgStringSupport
import com.github.tminglei.slickpg.utils.PlainSQLUtils._
import fusion.slick.pg.PgJacksonJsonSupport
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

import scala.concurrent.duration.FiniteDuration

trait FusionPostgresProfile
    extends ExPostgresProfile
    with PgStringSupport
    with PgArraySupport
    with PgDate2Support
    with PgJacksonJsonSupport
    with PgSearchSupport
    with PgRangeSupport
    with PgNetSupport
    with PgLTreeSupport
    with PgHStoreSupport
    with FusionJdbcProfile {
  driver: ExPostgresProfile =>

  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = MyAPI

  object MyAPI
      extends API
      with ArrayImplicits
      with DateTimeImplicits
      with JsonImplicits
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants
      with FusionImplicits {}

  val plainAPI = MyPlainAPI

  object MyPlainAPI
      extends SimpleArrayPlainImplicits
      with ByteaPlainImplicits
      with JacksonJsonPlainImplicits
      with Date2DateTimePlainImplicits
      with SimpleNetPlainImplicits
      with SimpleLTreePlainImplicits
      with SimpleRangePlainImplicits
      with SimpleHStorePlainImplicits
      with SimpleSearchPlainImplicits
      with FusionPlainImplicits {
    import scala.compat.java8.DurationConverters._
    implicit val getFiniteDuration = mkGetResult(_.nextDuration().toScala)
    implicit val getFiniteDurationOption = mkGetResult(_.nextDurationOption().map(_.toScala))
    implicit val setFiniteDuration = mkSetParameter[FiniteDuration]("interval", fd => fd.toJava.toString)
    implicit val setFiniteDurationOption = mkOptionSetParameter[FiniteDuration]("interval", fd => fd.toJava.toString)
    ///
    implicit val getPeriodArray = mkGetResult(_.nextArray[Period]())
    implicit val getPeriodArrayOption = mkGetResult(_.nextArrayOption[Period]())
    implicit val setPeriodArray = mkArraySetParameter[Period]("interval")
    implicit val setPeriodArrayOption = mkArrayOptionSetParameter[Period]("interval")
    ///
    implicit val getDurationArray = mkGetResult(_.nextArray[Duration]())
    implicit val getDurationArrayOption = mkGetResult(_.nextArrayOption[Duration]())
    implicit val setDurationArray = mkArraySetParameter[Duration]("interval")
    implicit val setDurationArrayOption = mkArrayOptionSetParameter[Duration]("interval")
    ///
    implicit val getFiniteDurationArray = mkGetResult(_.nextArray[FiniteDuration]())
    implicit val getFiniteDurationArrayOption = mkGetResult(_.nextArrayOption[FiniteDuration]())
    implicit val setFiniteDurationArray = mkArraySetParameter[FiniteDuration]("interval")
    implicit val setFiniteDurationArrayOption = mkArrayOptionSetParameter[FiniteDuration]("interval")
    ///
    implicit val getLocalDateArray = mkGetResult(_.nextArray[LocalDate]())
    implicit val getLocalDateArrayOption = mkGetResult(_.nextArrayOption[LocalDate]())
    implicit val setLocalDateArray = mkArraySetParameter[LocalDate]("date")
    implicit val setLocalDateArrayOption = mkArrayOptionSetParameter[LocalDate]("date")
    ///
    implicit val getLocalTimeArray = mkGetResult(_.nextArray[LocalTime]())
    implicit val getLocalTimeArrayOption = mkGetResult(_.nextArrayOption[LocalTime]())
    implicit val setLocalTimeArray = mkArraySetParameter[LocalTime]("time")
    implicit val setLocalTimeArrayOption = mkArrayOptionSetParameter[LocalTime]("time")
    ///
    implicit val getLocalDateTimeArray = mkGetResult(_.nextArray[LocalDateTime]())
    implicit val getLocalDateTimeArrayOption = mkGetResult(_.nextArrayOption[LocalDateTime]())
    implicit val setLocalDateTimeArray = mkArraySetParameter[LocalDateTime]("timestamp")
    implicit val setLocalDateTimeArrayOption = mkArrayOptionSetParameter[LocalDateTime]("timestamp")
    ///
    implicit val getInstantArray = mkGetResult(_.nextArray[Instant]())
    implicit val getInstantArrayOption = mkGetResult(_.nextArrayOption[Instant]())
    implicit val setInstantArray = mkArraySetParameter[Instant]("timestamp")
    implicit val setInstantArrayOption = mkArrayOptionSetParameter[Instant]("timestamp")
    ///
    implicit val getOffsetDateTimeArray = mkGetResult(_.nextArray[OffsetDateTime]())
    implicit val getOffsetDateTimeArrayOption = mkGetResult(_.nextArrayOption[OffsetDateTime]())
    implicit val setOffsetDateTimeArray = mkArraySetParameter[OffsetDateTime]("timestamptz")
    implicit val setOffsetDateTimeArrayOption = mkArrayOptionSetParameter[OffsetDateTime]("timestamptz")
    ///
    implicit val getZonedDateTimeArray = mkGetResult(_.nextArray[ZonedDateTime]())
    implicit val getZonedDateTimeArrayOption = mkGetResult(_.nextArrayOption[ZonedDateTime]())
    implicit val setZonedDateTimeArray = mkArraySetParameter[ZonedDateTime]("timestamptz")
    implicit val setZonedDateTimeArrayOption = mkArrayOptionSetParameter[ZonedDateTime]("timestamptz")
  }
}

object FusionPostgresProfile extends FusionPostgresProfile
