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

package akka.fusion

import java.util.concurrent.TimeoutException

import akka.actor.{ ActorSystem, ActorSystemImpl }

import scala.concurrent.Await
import scala.concurrent.duration._

object AkkaUtils {
  val AKKA_MANAGEMENT_FUSION = "akka.management.fusion"
  val AKKA_MANAGEMENT_FUSION_ENABLE = "akka.management.fusion.enable"

  /**
   * Shut down an actor system and wait for termination.
   * On failure debug output will be logged about the remaining actors in the system.
   *
   * If verifySystemShutdown is true, then an exception will be thrown on failure.
   */
  def shutdownActorSystem(
      actorSystem: ActorSystem,
      duration: Duration = 10.seconds,
      verifySystemShutdown: Boolean = false): Unit = {
    actorSystem.terminate()
    try Await.ready(actorSystem.whenTerminated, duration)
    catch {
      case _: TimeoutException =>
        val msg = "Failed to stop [%s] within [%s] \n%s".format(
          actorSystem.name,
          duration,
          actorSystem.asInstanceOf[ActorSystemImpl].printTree)
        if (verifySystemShutdown) throw new RuntimeException(msg)
        else println(msg)
    }
  }
}
