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

package fusion.http.gateway.server

import java.util.concurrent.ConcurrentHashMap

import akka.Done
import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.server.Route
import fusion.core.component.Components
import fusion.core.extension.FusionCore
import fusion.http.HttpSourceQueue
import helloscala.common.Configuration

import scala.concurrent.Future

class HttpGatewayComponents(system: ExtendedActorSystem) extends Components[Route]("fusion.http.default.gateway") {
  override def configuration: Configuration = FusionCore(system).configuration
  private val httpSourceQueueMap = new ConcurrentHashMap[(String, Int), HttpSourceQueue]()

  override protected def createComponent(id: String): Route = {
    val comp = configuration.get[Option[String]](s"$id.class") match {
      case Some(fqcn) =>
        system.dynamicAccess
          .createInstanceFor[HttpGatewayComponent](fqcn, List(classOf[String] -> id, classOf[ActorSystem] -> system))
          .getOrElse(throw new ExceptionInInitializerError(s"创建 HttpGatewayComponent 组件失败，fqdn: $fqcn"))
      case _ => new HttpGatewayComponent(id, system) {}
    }
    comp.route
  }

  override protected def componentClose(c: Route): Future[Done] = {
    import akka.http.scaladsl.util.FastFuture._
    import system.dispatcher
    var queues = List.empty[Future[Done]]
    httpSourceQueueMap.forEachValue(4, queue => {
      queue.complete()
      queues ::= queue.watchCompletion()
    })
    Future.sequence(queues).fast.map(_ => Done).fast.recover { case _ => Done }
  }
}
