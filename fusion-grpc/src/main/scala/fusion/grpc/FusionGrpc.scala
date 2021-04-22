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

package fusion.grpc

import akka.actor.typed.{ ActorSystem, Extension, ExtensionId }
import com.typesafe.scalalogging.StrictLogging
import fusion.cloud.FusionCloud
import fusion.cloud.discovery.{ FusionCloudDiscovery, Registration }

import scala.util.{ Failure, Success }

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date   2021-04-22 15:27:00
 */
class FusionGrpc(val system: ActorSystem[_]) extends Extension with StrictLogging {
  import system.executionContext
  val cloud: FusionCloud = FusionCloud(system)
  import cloud.timeout

  cloud.entityRefs.cloudDiscovery
    .askWithStatus[Registration](replyTo => FusionCloudDiscovery.GetRegistration(replyTo))
    .onComplete {
      case Success(registration) =>
        cloud.entityRefs.cloudDiscovery ! FusionCloudDiscovery.AddRegistration(registration)
      case Failure(e) =>
        logger.error("Failed to get the Registration object.", e)
    }
}

object FusionGrpc extends ExtensionId[FusionGrpc] {
  override def createExtension(system: ActorSystem[_]): FusionGrpc = new FusionGrpc(system)
}
