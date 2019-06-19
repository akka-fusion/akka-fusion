package fusion.log.logback

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class LogServiceNameConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = {
    Option(System.getProperty("fusion.discovery.nacos.serviceName")).getOrElse(System.getProperty("fusion.name"))
  }
}
