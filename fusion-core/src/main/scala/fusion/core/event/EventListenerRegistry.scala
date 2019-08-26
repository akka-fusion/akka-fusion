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

package fusion.core.event

import java.util.Objects

trait EventListenerRegistry[E <: AnyRef] {
  protected var _event: E = _
  protected var _listeners = List[E => Unit]()

  def addListener(listener: E => Unit): Unit = synchronized {
    if (Objects.nonNull(_event)) {
      listener(_event)
    }
    _listeners ::= listener
  }

  def complete(event: E): Unit = synchronized {
    if (Objects.nonNull(_event)) {
      throw new IllegalStateException(s"$event 只能被调用一次")
    }
    _event = event
    dispatch()
  }

  protected def dispatch(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = _listeners.foreach(_.apply(_event))
    }).start()
  }
}
