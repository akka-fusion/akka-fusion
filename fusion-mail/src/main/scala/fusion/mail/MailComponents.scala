package fusion.mail

import java.util.Properties

import fusion.core.util.Components
import fusion.mail.constant.MailConstants
import helloscala.common.Configuration

class MailComponents(val config: Configuration) extends Components[MailHelper](s"${MailConstants.CONF_ROOT}.default") {
  override protected def createComponent(id: String): MailHelper = {
    new MailHelper(config.get[Properties](id), null)
  }

  override protected def componentClose(c: MailHelper): Unit = c.close()

}
