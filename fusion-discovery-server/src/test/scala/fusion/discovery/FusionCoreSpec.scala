/*
 * Copyright 2019 akka-fusion.com
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

package fusion.discovery

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed._
import akka.util.Timeout
import fusion.test.FusionTestWordSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

object SampleActor {
  trait Command

  def apply(): Behavior[Command] = Behaviors.receiveMessagePartial {
    case msg =>
      println(msg)
      Behaviors.same
  }
}

class FusionCoreSpec extends FusionTestWordSpec with BeforeAndAfterAll with Matchers {
  val system = ActorSystem(SpawnProtocol(), "test")

  "FusionCore" should {
//    "test" in {
//      val fusionGuardian = FusionCore(system).fusionGuardian
//      println(fusionGuardian)
//
//      val ref = FusionCore(system).spawnActorSync(FusionProtocol.behavior, "test", 2.seconds)
//      println(ref)
//    }

    "spawn" in {
      implicit val timeout = Timeout(2.seconds)
      implicit val scheduler = system.scheduler

      val spawnActor: ActorRef[SpawnProtocol.Command] = system.toClassic
        .actorOf(PropsAdapter(Behaviors.supervise(SpawnProtocol()).onFailure(SupervisorStrategy.resume)), "spawn")
        .toTyped[SpawnProtocol.Command]

      val sampleActorF: Future[ActorRef[SampleActor.Command]] = spawnActor.ask[ActorRef[SampleActor.Command]](replyTo =>
        SpawnProtocol.Spawn(SampleActor(), "sample", Props.empty, replyTo))

      val sampleActor: ActorRef[SampleActor.Command] = Await.result(sampleActorF, 2.seconds)

      println(sampleActor)
    }
  }

  override protected def afterAll(): Unit = { system.terminate() }
}
