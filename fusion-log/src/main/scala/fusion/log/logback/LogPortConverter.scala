package fusion.log.logback

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import fusion.core.constant.FusionConstants

class LogPortConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = System.getProperty(FusionConstants.SERVER_PORT_PATH)
}
