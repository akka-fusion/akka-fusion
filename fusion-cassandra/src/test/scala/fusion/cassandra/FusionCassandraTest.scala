package fusion.cassandra

import java.time.Instant
import java.time.LocalDate

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite

class FusionCassandraTest extends TestKit(ActorSystem("cassandra-test")) with FusionTestFunSuite {
  test("FusionCassandra") {
    val session = FusionCassandra(system).component
    val pstmt   = session.prepare("insert into l_basic(imei, day, i, o, p, s, u) values(:imei, :day, :i, :o, :p, :s, :u)")
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
