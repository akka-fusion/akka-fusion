package fusion.log.logback

import java.net.InetAddress

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class LogHostNameConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String =
    try {
      InetAddress.getLocalHost.getCanonicalHostName
    } catch {
      case _: Throwable =>
        ""
    }
}
