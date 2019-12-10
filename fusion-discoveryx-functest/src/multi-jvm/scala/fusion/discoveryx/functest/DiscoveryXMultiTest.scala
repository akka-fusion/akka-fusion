package fusion.discoveryx.functest

import akka.remote.testconductor.RoleName
import akka.remote.testkit.{MultiNodeConfig, STMultiNodeSpec, SchudulerXMultiNodeSpec}
import com.typesafe.config.ConfigFactory
import fusion.discoveryx.DiscoveryX
import fusion.discoveryx.client.DiscoveryXNamingClient
import fusion.discoveryx.common.Constants
import fusion.discoveryx.model.{InstanceQuery, InstanceRegister, ServerStatusQuery}
import fusion.discoveryx.server.DiscoveryXServer
import helloscala.common.IntStatus

object DiscoveryXMultiTestConfig extends MultiNodeConfig {
  val servers: Vector[RoleName] = (0 until 1).map(i => role(s"server$i")).toVector

  val clients: Vector[RoleName] = (0 until 1).map(i => role(s"client$i")).toVector

  private def makeSeedNodes(): String = {
    servers.indices.map(i => "127.0.0.1:" + (49001 + i)).mkString("[\"", "\",\"", "\"]")
  }

  // this configuration will be used for all nodes
  // note that no fixed hostname names and ports are used
  commonConfig(ConfigFactory.parseString(s"""
    akka.loglevel = "DEBUG"
    akka.actor.provider = cluster
    discoveryx.name = discoveryx
    """).withFallback(ConfigFactory.load()))

  for ((node, idx) <- servers.zipWithIndex) {
    nodeConfig(node)(ConfigFactory.parseString(s"""fusion.http.default.server.port = ${48000 + idx}
      |discoveryx {
      |  akka.remote.artery.canonical.port = ${49001 + idx}
      |  akka.cluster.roles = [${Constants.NODE_SERVER}]
      |  akka.cluster.seed-nodes = ${makeSeedNodes()}
      |}""".stripMargin))
  }

  for ((node, idx) <- clients.zipWithIndex) {
    nodeConfig(node)(ConfigFactory.parseString(s"""discoveryx {
      |  akka.cluster.roles = []
      |  akka.remote.artery.canonical.port = ${49004 + idx}
      |  akka.grpc.client {
      |    "*" {
      |      use-tls = false
      |      host = "127.0.0.1"
      |      port = ${48000 + idx}
      |    }
      |    "fusion.discoveryx.grpc.ConfigService" {
      |    }
      |    "fusion.discoveryx.grpc.NamingService" {
      |    }
      |  }
      |}""".stripMargin))
  }
}

abstract class DiscoveryXMultiTest
    extends SchudulerXMultiNodeSpec(DiscoveryXMultiTestConfig, config => DiscoveryX.fromOriginalConfig(config))
    with STMultiNodeSpec {
  import DiscoveryXMultiTestConfig._

  private val namespace = "scala-meetup"
  private val serviceName = "akka"
  private val groupName = "default"

  "FusionDiscovery" must {
    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "start service" in {
      runOn(servers: _*) {
        DiscoveryXServer(discoveryX).start()
        enterBarrier("server-startup")
      }
      for ((role, idx) <- clients.zipWithIndex) {
        runOn(role) {
          enterBarrier("server-startup")
          val namingClient = DiscoveryXNamingClient(discoveryX.system)
          val in =
            InstanceRegister(namespace, serviceName, groupName, s"127.0.0.${200 + idx}", 50000 + idx, healthy = true)
          namingClient.registerInstance(in)
        }
      }

      enterBarrier("start-finished")
    }

    "naming-client" in {
      runOn(clients: _*) {
        val namingClient = DiscoveryXNamingClient(discoveryX.system)
        namingClient.serverStatus(ServerStatusQuery()).futureValue.status should be(IntStatus.OK)

        val result = namingClient.queryInstance(InstanceQuery(namespace, serviceName, groupName, allHealthy = true)).futureValue
        println(s"Query Instance return is: $result")
        result.status should be(IntStatus.OK)
        result.data.queried should not be empty
      }
    }
  }
}

class DiscoveryXMultiTestMultiJvmNode1 extends DiscoveryXMultiTest
class DiscoveryXMultiTestMultiJvmNode2 extends DiscoveryXMultiTest
//class DiscoveryXMultiTestMultiJvmNode3 extends DiscoveryXMultiTest
//class DiscoveryXMultiTestMultiJvmNode4 extends DiscoveryXMultiTest
//class DiscoveryXMultiTestMultiJvmNode5 extends DiscoveryXMultiTest
//class DiscoveryXMultiTestMultiJvmNode6 extends DiscoveryXMultiTest
