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

package fusion.mail

import java.util.Properties

import akka.Done
import fusion.core.component.Components
import fusion.mail.constant.MailConstants
import helloscala.common.Configuration

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MailComponents(val configuration: Configuration)
    extends Components[MailHelper](s"${MailConstants.CONF_ROOT}.default") {
  override protected def createComponent(id: String): MailHelper = {
    new MailHelper(configuration.get[Properties](id), null)
  }

  override protected def componentClose(c: MailHelper): Future[Done] = Future {
    c.close()
    Done
  }

}
