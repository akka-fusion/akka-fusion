package fusion.http.server

import java.nio.file.Paths
import java.security.{KeyStore, SecureRandom}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.{handleExceptions, handleRejections}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.StrictLogging
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import fusion.core.constant.FusionConstants
import helloscala.common.Configuration
import helloscala.common.util.{PidFile, StringUtils, Utils}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

trait AkkaHttpServer extends BaseExceptionPF with BaseRejectionBuilder with StrictLogging {
  val system: ActorSystem

  val materializer: ActorMaterializer

  val hlServerValue: String

  def configuration: Configuration

  def routes: Route

  def close(): Unit = {}

  protected var bindingFuture: Future[ServerBinding] = _
  protected var httpsBindingFuture: Option[Future[ServerBinding]] = _
  private var _serverHost: String = ""
  private var _serverPort: Int = -1
  private var _serverPortSsl: Int = -1
  def serverHost: String = _serverHost
  def serverPort: Int = _serverPort
  def serverPortSsl: Int = _serverPortSsl

  final def shutdown(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val f = bindingFuture.flatMap { binding =>
      logger.info(s"Unbind: $binding")
      binding.unbind()
    }
    val fSsl = httpsBindingFuture.map(_.flatMap { binding =>
      logger.info(s"Unbind: $binding")
      binding.unbind()
    })
    try {
      Await.result(f, 60.seconds)
    } catch {
      case e: Exception =>
        logger.error("停止http服务错误", e)
    }
    fSsl.foreach { ff =>
      try {
        Await.result(ff, 60.seconds)
      } catch {
        case e: Exception =>
          logger.error("停止https服务错误", e)
      }
    }
    close()
  }

  def handleMapResponse(response: HttpResponse): HttpResponse = {
    val name = hlServerValue + "/" + _serverHost + ":" + _serverPort
    val headers = RawHeader(FusionConstants.HEADER_NAME, name) +: response.headers
    response.copy(headers = headers)
  }

  def afterHttpBindingSuccess(binding: ServerBinding): Unit =
    logger.info(
      s"Server online at http://${binding.localAddress.getAddress.getHostAddress}:${binding.localAddress.getPort}/")

  def afterHttpsBindingSuccess(binding: ServerBinding): Unit =
    logger.info(s"Server online at https://${binding.localAddress.getAddress}:${binding.localAddress.getPort}/")

  def afterHttpBindingFailure(cause: Throwable): Unit = {
    logger.error(s"Error starting the server ${cause.getMessage}", cause)
    close()
    System.exit(-1)
  }

  def afterHttpsBindingFailure(cause: Throwable): Unit = {
    logger.error(s"Error starting the server ${cause.getMessage}", cause)
    close()
    System.exit(-1)
  }

  protected def generateHttps(): HttpsConnectionContext = {
    var hcc: HttpsConnectionContext = null
    try {
      val password = configuration.getString("ssl-config.password").toCharArray
      //    val password = "abcdef".toCharArray // do not store passwords in code, read them from somewhere safe!

      val ks = KeyStore.getInstance("PKCS12")
      val keystore =
        getClass.getClassLoader.getResourceAsStream("ssl-keys/server.p12")

      require(keystore != null, "Keystore required!")
      ks.load(keystore, password)

      val keyManagerFactory: KeyManagerFactory =
        KeyManagerFactory.getInstance("SunX509")
      keyManagerFactory.init(ks, password)

      val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
      tmf.init(ks)

      val sslContext: SSLContext = SSLContext.getInstance("TLS")
      sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
      hcc = ConnectionContext.https(sslContext, Some(AkkaSSLConfig(system)))
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        System.exit(-1)
    }
    hcc
  }

  def startServerAwait(): Unit =
    Await.result(startServer()._1, 60.seconds)

  /**
   * 启动基于Akka HTTP的服务
   *
   * @return (http绑定，https绑定)
   */
  def startServer(): (Future[ServerBinding], Option[Future[ServerBinding]])

  /**
   * 启动基于Akka HTTP的服务
   *
   * @param prefix 配置前缀，需要包含尾部的点符号。如：mass.job应用设置为 mass.job.
   * @return (http绑定，https绑定)
   */
  def startServer(prefix: String): (Future[ServerBinding], Option[Future[ServerBinding]]) =
    startServer(
      configuration.getString(s"${prefix}server.host"),
      configuration.getInt(s"${prefix}server.port"),
      configuration.get[Option[Int]](s"${prefix}server.https-port")
    )

  /**
   * 根据设置的host:绑定主机名和port:绑定网络端口 启动Akka HTTP服务
   */
  def startServer(
      host: String,
      port: Int,
      httpsPort: Option[Int]): (Future[ServerBinding], Option[Future[ServerBinding]]) = {
    implicit val _system: ActorSystem = system
    implicit val _mat: ActorMaterializer = materializer
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    writePidfile()

    val flow: Flow[HttpRequest, HttpResponse, Any] =
      (handleRejections(rejectionHandler) &
        handleExceptions(exceptionHandler)) {
        routes
      }
    val handler = flow.map(handleMapResponse)

    bindingFuture = Http().bindAndHandle(handler, interface = host, port = port, settings = ServerSettings(system))

    bindingFuture.onComplete {
      case Success(binding) =>
        _serverHost = binding.localAddress.getAddress.getHostAddress
        _serverPort = binding.localAddress.getPort
        afterHttpBindingSuccess(binding)
      case Failure(cause) =>
        afterHttpBindingFailure(cause)
    }

    httpsBindingFuture = httpsPort.map { portSsl =>
      val f = Http().bindAndHandle(handler,
                                   interface = host,
                                   port = portSsl,
                                   connectionContext = generateHttps(),
                                   settings = ServerSettings(system))
      f.onComplete {
        case Success(binding) =>
          _serverPortSsl = binding.localAddress.getPort
          afterHttpsBindingSuccess(binding)
        case Failure(cause) =>
          afterHttpsBindingFailure(cause)
      }
      f
    }

    sys.addShutdownHook { shutdown() }

    bindingFuture -> httpsBindingFuture
  }

  private def writePidfile(): Unit = {
    val pidfilePath = Option(System.getProperty("pidfile.path"))
      .orElse(configuration.get[Option[String]]("pidfile.path"))
      .orNull
    try {
      if (StringUtils.isNoneBlank(pidfilePath)) {
        PidFile(Utils.getPid)
          .create(Paths.get(pidfilePath), deleteOnExit = true)
      } else {
        logger.warn("-Dpidfile.path 未设置，将不写入 .pid 文件。")
      }
    } catch {
      case NonFatal(e) =>
        logger.error(s"将进程ID写入文件：$pidfilePath 失败", e)
        System.exit(-1)
    }
  }

}
