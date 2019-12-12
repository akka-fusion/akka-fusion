package fusion.schedulerx.functest

import java.util.concurrent.TimeUnit

import akka.remote.testconductor.RoleName
import akka.remote.testkit.{ MultiNodeConfig, STMultiNodeSpec, SchudulerXMultiNodeSpec }
import com.typesafe.config.ConfigFactory
import fusion.schedulerx.{ SchedulerX, server }
import fusion.schedulerx.server.SchedulerXBroker
import fusion.schedulerx.worker.SchedulerXWorker

object SchedulerXMultiTestConfig extends MultiNodeConfig {
  val brokers: Vector[RoleName] = (0 until 1).map(i => role(s"broker$i")).toVector

  val workers: Vector[RoleName] = (0 until 3).map(i => role(s"worker$i")).toVector

  private def makeSeedNodes(): String = {
    brokers.indices.map(i => "127.0.0.1:" + (9000 + i)).mkString("[\"", "\",\"", "\"]")
  }

  // this configuration will be used for all nodes
  // note that no fixed hostname names and ports are used
  commonConfig(ConfigFactory.parseString(s"""
    akka.loglevel = "DEBUG"
    akka.actor.provider = cluster
    akka.cluster.seed-nodes = ${makeSeedNodes()}
    """).withFallback(ConfigFactory.load()))

  for ((node, idx) <- brokers.zipWithIndex) {
    nodeConfig(node)(ConfigFactory.parseString(s"""schedulerx {
      |  name = schedulerx
      |  akka.remote.artery.canonical.port = ${9000 + idx}
      |  akka.cluster.roles = [broker]
      |}""".stripMargin))
  }

  for ((node, idx) <- workers.zipWithIndex) {
    nodeConfig(node)(ConfigFactory.parseString(s"""schedulerx {
        |  name = schedulerx
        |  akka.remote.artery.canonical.port = ${9090 + idx}
        |  akka.cluster.roles = [worker]
        |}""".stripMargin))
  }
}

abstract class SchedulerXMultiTest
    extends SchudulerXMultiNodeSpec(SchedulerXMultiTestConfig, config => SchedulerX.fromOriginalConfig(config))
    with STMultiNodeSpec {
  import SchedulerXMultiTestConfig._

  private var schedulerXWorkers = Vector.empty[SchedulerXWorker]
  private var schedulerXBrokers = Vector.empty[SchedulerXBroker]

  "SchedulerX" must {
    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "wait for all nodes initialize finished" in {
      runOn(brokers: _*) {
        schedulerXBrokers :+= server.SchedulerXBroker(schedulerX).start()
        enterBarrier("brokers-init")
      }

      runOn(workers: _*) {
        enterBarrier("brokers-init")
        schedulerXWorkers :+= SchedulerXWorker(schedulerX).start()
      }

      enterBarrier("finished")
    }

    "timeout" in {
      TimeUnit.SECONDS.sleep(14)
      enterBarrier("timeout")
    }
  }
}

class SchedulerXMultiTestMultiJvmNode1 extends SchedulerXMultiTest
class SchedulerXMultiTestMultiJvmNode2 extends SchedulerXMultiTest
class SchedulerXMultiTestMultiJvmNode3 extends SchedulerXMultiTest
class SchedulerXMultiTestMultiJvmNode4 extends SchedulerXMultiTest
//class SchedulerXMultiTestMultiJvmNode5 extends SchedulerXMultiTest
//class SchedulerXMultiTestMultiJvmNode6 extends SchedulerXMultiTest
//class SchedulerXMultiTestMultiJvmNode7 extends SchedulerXMultiTest
