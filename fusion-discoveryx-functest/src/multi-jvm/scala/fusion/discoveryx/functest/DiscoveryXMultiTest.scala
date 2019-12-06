package fusion.discoveryx.functest

import akka.remote.testconductor.RoleName
import akka.remote.testkit.{ MultiNodeConfig, STMultiNodeSpec, SchudulerXMultiNodeSpec }
import com.typesafe.config.ConfigFactory
import fusion.discoveryx.DiscoveryX
import fusion.discoveryx.common.Constants
import fusion.discoveryx.server.DiscoveryXServer

object DiscoveryXMultiTestConfig extends MultiNodeConfig {
  val servers: Vector[RoleName] = (0 until 3).map(i => role(s"server$i")).toVector

  private def makeSeedNodes(): String = {
    servers.indices.map(i => "127.0.0.1:" + (49001 + i)).mkString("[\"", "\",\"", "\"]")
  }

  // this configuration will be used for all nodes
  // note that no fixed hostname names and ports are used
  commonConfig(ConfigFactory.parseString(s"""
    akka.loglevel = "DEBUG"
    akka.actor.provider = cluster
    akka.cluster.seed-nodes = ${makeSeedNodes()}
    """).withFallback(ConfigFactory.load()))

  for ((node, idx) <- servers.zipWithIndex) {
    nodeConfig(node)(ConfigFactory.parseString(s"""fusion.http.default.server.port = ${48000 + idx}
      |discoveryx {
      |  name = discoveryx
      |  akka.remote.artery.canonical.port = ${49001 + idx}
      |  akka.cluster.roles = [${Constants.NODE_SERVER}]
      |}""".stripMargin))
  }
}

abstract class DiscoveryXMultiTest
    extends SchudulerXMultiNodeSpec(DiscoveryXMultiTestConfig, config => DiscoveryX.fromOriginalConfig(config))
    with STMultiNodeSpec {
  import DiscoveryXMultiTestConfig._

  "FusionDiscovery" must {
    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
      DiscoveryXServer(discoveryX).start()
    }

    "finished" in {
      enterBarrier("finished")
    }
  }
}

class DiscoveryXMultiTestMultiJvmNode1 extends DiscoveryXMultiTest
class DiscoveryXMultiTestMultiJvmNode2 extends DiscoveryXMultiTest
class DiscoveryXMultiTestMultiJvmNode3 extends DiscoveryXMultiTest
