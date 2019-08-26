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

package fusion.cassandra

import java.time.Instant
import java.time.LocalDate

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite

class FusionCassandraTest extends TestKit(ActorSystem("cassandra-test")) with FusionTestFunSuite {
  test("FusionCassandra") {
    val session = FusionCassandra(system).component
    val pstmt = session.prepare("insert into l_basic(imei, day, i, o, p, s, u) values(:imei, :day, :i, :o, :p, :s, :u)")
    val stmt = pstmt
      .bind()
      .setString("imei", "imei")
      .setLocalDate("day", LocalDate.now())
      .setInstant("i", Instant.now())
      .setDouble("o", 22.22)
      .setDouble("u", 22.22)
      .setInt("p", 99)
      .setInt("s", 99)
    session.execute(stmt)
  }

}
