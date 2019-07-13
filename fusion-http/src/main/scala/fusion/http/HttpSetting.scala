package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.UseHttp2
import com.typesafe.sslconfig.ssl.SSLConfigFactory
import com.typesafe.sslconfig.ssl.SSLConfigSettings
import fusion.common.constant.FusionConstants
import fusion.http.constant.HttpConstants
import helloscala.common.Configuration

class HttpSetting(configuration: Configuration, system: ActorSystem) {
  def exceptionHandlerOption: Option[String] = configuration.get[Option[String]]("exception-handler")
  def rejectionHandlerOption: Option[String] = configuration.get[Option[String]]("rejection-handler")
  def defaultFilter: String                  = configuration.getString("default-filter")
  def httpFilters: Seq[String]               = configuration.get[Seq[String]]("http-filters")

  def http2: UseHttp2 = configuration.getOrElse("http2", "").toLowerCase match {
    case "never"  => UseHttp2.Never
    case "always" => UseHttp2.Always
    case _        => UseHttp2.Negotiated
  }

  def createSSLConfig(): SSLConfigSettings = {
    val akkaOverrides = system.settings.config.getConfig("akka.ssl-config")
    val defaults      = system.settings.config.getConfig("ssl-config")
    val mergeConfig   = akkaOverrides.withFallback(defaults)
    val sslConfig = if (configuration.hasPath("ssl.ssl-config")) {
      configuration.getConfig("ssl.ssl-config").withFallback(mergeConfig)
    } else {
      mergeConfig
    }
    SSLConfigFactory.parse(sslConfig)
  }

  object server {

    def host: String =
      configuration.getOrElse(
        FusionConstants.SERVER_HOST_PATH,
        system.settings.config.getString(HttpConstants.SERVER_HOST_PATH))

    def port: Int = {
      configuration.get[Option[Int]](FusionConstants.SERVER_PORT_PATH) match {
        case Some(port) => port
        case _ =>
          if (!system.settings.config.hasPath(HttpConstants.SERVER_PORT_PATH)) 0
          else system.settings.config.getInt(HttpConstants.SERVER_PORT_PATH)
      }
    }
  }
}
