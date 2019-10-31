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

package sample.stats

import com.typesafe.config.ConfigFactory
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Routers
import akka.cluster.typed.Cluster

import scala.concurrent.duration._

object App {

  val StatsServiceKey = ServiceKey[StatsService.ProcessText]("StatsService")

  object RootBehavior {

    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
      val cluster = Cluster(ctx.system)
      if (cluster.selfMember.hasRole("compute")) {
        // on every compute node there is one service instance that delegates to N local workers
        val numberOfWorkers = ctx.system.settings.config.getInt("stats-service.workers-per-node")
        val workers = ctx.spawn(Routers.pool(numberOfWorkers)(StatsWorker()), "WorkerRouter")
        val service = ctx.spawn(StatsService(workers), "StatsService")

        // published through the receptionist to the other nodes in the cluster
        ctx.system.receptionist ! Receptionist.Register(StatsServiceKey, service)
      }
      if (cluster.selfMember.hasRole(("client"))) {
        val serviceRouter = ctx.spawn(Routers.group(App.StatsServiceKey), "ServiceRouter")
        ctx.spawn(StatsSampleClient(serviceRouter), "Client")
      }
      Behaviors.empty[Nothing]
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startup("compute", 25251)
      startup("compute", 25252)
      startup("compute", 0)
      startup("client", 0)
    } else {
      require(args.size == 2, "Usage: role port")
      startup(args(0), args(1).toInt)
    }
  }

  def startup(role: String, port: Int): Unit = {

    // Override the configuration of the port when specified as program argument
    val config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """).withFallback(ConfigFactory.load("stats"))

    val system = ActorSystem[Nothing](RootBehavior(), "ClusterSystem", config)
  }
}

object StatsSampleClient {

  sealed trait Event
  private case object Tick extends Event
  private case class ServiceResponse(result: StatsService.Response) extends Event

  def apply(service: ActorRef[StatsService.ProcessText]): Behavior[Event] =
    Behaviors.setup { ctx =>
      Behaviors.withTimers { timers =>
        timers.startTimerWithFixedDelay(Tick, Tick, 2.seconds)
        val responseAdapter = ctx.messageAdapter(ServiceResponse)

        Behaviors.receiveMessage {
          case Tick =>
            ctx.log.info("Sending process request")
            service ! StatsService.ProcessText("this is the text that will be analyzed", responseAdapter)
            Behaviors.same
          case ServiceResponse(result) =>
            ctx.log.info("Service result: {}", result)
            Behaviors.same
        }
      }
    }

}
