package akka.remote.testkit

import akka.actor.typed.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import fusion.discoveryx.DiscoveryX
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.implicitConversions

trait STMultiNodeSpec
    extends MultiNodeSpecCallbacks
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {
  self: SchudulerXMultiNodeSpec =>

  override def initialParticipants: Int = roles.size

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5000, Millis)), scaled(Span(50, Millis)))

  def typedSystem: ActorSystem[_] = discoveryX.system

  override def beforeAll(): Unit = multiNodeSpecBeforeAll()

  override def afterAll(): Unit = multiNodeSpecAfterAll()

  // Might not be needed anymore if we find a nice way to tag all logging from a node
  implicit override def convertToWordSpecStringWrapper(s: String): WordSpecStringWrapper =
    new WordSpecStringWrapper(s"$s (on node '${self.myself.name}', $getClass)")
}

abstract class SchudulerXMultiNodeSpec(nodeConfig: MultiNodeConfig, protected val discoveryX: DiscoveryX)
    extends MultiNodeSpec(nodeConfig.myself, discoveryX.classicSystem, nodeConfig.roles, nodeConfig.deployments) {
  def this(nodeConfig: MultiNodeConfig, create: Config => DiscoveryX) {
    this(nodeConfig, create(ConfigFactory.load(nodeConfig.config)))
  }
}
