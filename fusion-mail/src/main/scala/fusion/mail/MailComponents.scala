package fusion.mail

import java.util.Properties

import akka.Done
import fusion.core.util.Components
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
