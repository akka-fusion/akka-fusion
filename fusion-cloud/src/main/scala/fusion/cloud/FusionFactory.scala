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

package fusion.cloud

import akka.actor.typed.{ActorSystem, Behavior}

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-12-01 23:40:06
 */
trait FusionFactory extends FusionActorSystemFactory {
  def actorSystem: ActorSystem[_]
}

trait FusionActorSystemFactory {
  private[fusion] def createActorSystem[T](guardianBehavior: Behavior[T]): ActorSystem[T]

  def initActorSystem[T](guardianBehavior: Behavior[T]): ActorSystem[T]
}
