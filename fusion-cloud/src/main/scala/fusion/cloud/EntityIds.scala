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

package fusion.cloud

import akka.actor.typed.ActorSystem
import akka.cluster.MemberStatus
import akka.cluster.typed.Cluster

import scala.util.Random

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-12-01 23:29:21
 */
object EntityIds {
  def entityId: String = "default"

  def entityId(system: ActorSystem[_]): String = "id-" + Random.nextInt(memberOnLimit(system))

  def memberOnLimit(system: ActorSystem[_]): Int = {
    val n = Cluster(system).state.members.count(_.status == MemberStatus.Up)
    if (n == 0) 1 else n
  }
}
