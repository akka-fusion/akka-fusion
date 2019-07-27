package fusion.http

import java.net.Inet4Address
import java.net.InetSocketAddress
import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.ConnectionContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.HttpConnectionContext
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import fusion.core.event.http.HttpBindingServerEvent
import fusion.core.extension.FusionCore
import fusion.http.server.BaseRejectionBuilder
import fusion.http.server.FusionRejectionHandler
import fusion.http.server.HttpThrowableFilter
import fusion.http.server.HttpThrowableFilter.ThrowableFilter
import fusion.http.util.HttpUtils
import helloscala.common.Configuration
import helloscala.common.exception.HSInternalErrorException
import helloscala.common.util.NetworkUtils

import scala.concurrent.Await
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

final class HttpServer(val id: String, val system: ExtendedActorSystem) extends StrictLogging with AutoCloseable {
  implicit private def _system: ActorSystem         = system
  implicit private val mat: ActorMaterializer       = ActorMaterializer()
  implicit private def ec: ExecutionContextExecutor = mat.executionContext
  private val _isStarted                            = new AtomicBoolean(false)
  @volatile private var _isRunning                  = false
  private val c                                     = Configuration(system.settings.config.getConfig(id))
  private var _socketAddress: InetSocketAddress     = _
  private var maybeEventualBinding                  = Option.empty[Future[ServerBinding]]
  private val httpSetting                           = new HttpSetting(c, system)

  def startRouteSync(route: Route)(
      implicit rh: RejectionHandler = null,
      htf: HttpThrowableFilter = null,
      duration: Duration = 30.seconds): ServerBinding =
    Await.result(startRouteAsync(route), duration)

  def startRouteAsync(
      route: Route)(implicit rh: RejectionHandler = null, htf: HttpThrowableFilter = null): Future[ServerBinding] = {
    val rejectionHandler     = createRejectionHandler(rh)
    val maybeThrowableFilter = createThrowableFilter(htf)

    val exceptionHandler = maybeThrowableFilter
      .map(HttpThrowableFilter.createExceptionHandler)
      .getOrElse(HttpThrowableFilter.exceptionHandlerPF)
    val throwableFilter = maybeThrowableFilter.getOrElse(HttpThrowableFilter.defaultThrowableFilter)

    val handler          = routeToHandler(route, rejectionHandler, exceptionHandler)
    val effectiveHandler = generateHttpHandler(handler, throwableFilter)
    startAsync(effectiveHandler, generateConnectionContext())
  }

  private def routeToHandler(route: Route, rh: RejectionHandler, eh: ExceptionHandler): HttpHandler = {
    implicit val _rh: RejectionHandler = rh
    implicit val _eh: ExceptionHandler = eh
    Route.asyncHandler(route)
  }

  private def createThrowableFilter(httpExceptionHandler: HttpThrowableFilter): Option[ThrowableFilter] = {
    Option(httpExceptionHandler).map(_.throwableFilter).orElse {
      httpSetting.exceptionHandlerOption.map { clz =>
        system.dynamicAccess.createInstanceFor[HttpThrowableFilter](clz, Nil) match {
          case Success(eh) => eh.throwableFilter
          case Failure(e) =>
            throw new ExceptionInInitializerError(
              s"$clz 不是有效的 fusion.http.server.FusionExceptionHandler，${e.getLocalizedMessage}")
        }
      }
    }
//      .map(HttpThrowableFilter.createExceptionHandler)
//      .getOrElse(HttpThrowableFilter.exceptionHandlerPF)
  }

  private def createRejectionHandler(httpRejectionHandler: RejectionHandler): RejectionHandler = {
    Option(httpRejectionHandler)
      .orElse {
        httpSetting.rejectionHandlerOption.map { clz =>
          system.dynamicAccess.createInstanceFor[FusionRejectionHandler](clz, Nil) match {
            case Success(rh) => rh.rejectionHandler
            case Failure(e) =>
              throw new ExceptionInInitializerError(
                s"$clz 不是有效的 fusion.http.server.FusionRejectionHandler，${e.getLocalizedMessage}")
          }
        }
      }
      .getOrElse(BaseRejectionBuilder.rejectionHandler)
  }

  /**
   * 阻塞调用
   */
  @throws(classOf[Exception])
  def startHandlerSync(
      handler: HttpHandler)(implicit htf: HttpThrowableFilter = null, duration: Duration = 30.seconds): ServerBinding =
    Await.result(startHandlerAsync(handler), duration)

  def startHandlerAsync(handler: HttpHandler)(implicit htf: HttpThrowableFilter = null): Future[ServerBinding] = {
    val exceptionFilter = createThrowableFilter(htf).getOrElse(HttpThrowableFilter.defaultThrowableFilter)
    startAsync(generateHttpHandler(handler, exceptionFilter), generateConnectionContext())
  }

  private def startAsync(handler: HttpHandler, connectionContext: ConnectionContext): Future[ServerBinding] = {
    if (!_isStarted.compareAndSet(false, true)) {
      throw HSInternalErrorException("HttpServer只允许start一次")
    }

    val realHandler = getDefaultFilter().execute(handler, system)
    val bindingFuture =
      Http().bindAndHandleAsync(realHandler, httpSetting.server.host, httpSetting.server.port, connectionContext)

    maybeEventualBinding = Some(bindingFuture)
    bindingFuture.failed.foreach { cause =>
      afterHttpBindingFailure(cause, connectionContext.isSecure)
    }
    bindingFuture.map { binding =>
      afterHttpBindingSuccess(binding, connectionContext.isSecure)
      binding
    }
  }

  private def generateHttpHandler(handler: HttpHandler, throwableFilter: ThrowableFilter): HttpHandler = {
    // XXX 这里需要反转列表，因为函数调用时最外层最先调用
    val chains = getHttpFilters().reverse

//    val r: HttpHandler = (request: HttpRequest) => {
//      val (req3, funcs) = chains.foldLeft((request, List.empty[HttpResponse => Future[HttpResponse]])) {
//        case ((req, resps), filter) =>
//          val (req2, func) = filter.filter(req)
//          (req2, func :: resps)
//      }
//
//      handler(req3).flatMap { resp =>
//        val result = funcs.foldLeft(Future.successful(resp))((rF, func) => rF.flatMap(r => func(r)))
//        result
//      }
//    }

    val r: HttpHandler = req =>
      try {
        val h = chains.foldLeft(handler) { (hh, filter) =>
          filter.execute(hh, system)
        }
        h(req).recoverWith(throwableFilter)
      } catch {
        case e: Throwable if throwableFilter.isDefinedAt(e) => throwableFilter(e)
        case e: Throwable                                   => Future.failed(e)
      }
    r
  }

  private def getDefaultFilter(): HttpFilter = createHttpFilter(httpSetting.defaultFilter).get

  private def getHttpFilters(): Seq[HttpFilter] = {
    httpSetting.httpFilters.flatMap(className => createHttpFilter(className))
  }

  private def createHttpFilter(className: String): Option[HttpFilter] = {
    val clz = Class.forName(className)
    if (classOf[AbstractHttpFilter].isAssignableFrom(clz)) {
      system.dynamicAccess
        .createInstanceFor[AbstractHttpFilter](clz, List(classOf[ExtendedActorSystem] -> system)) match {
        case Success(v) => Some(v)
        case Failure(e) =>
          logger.error(s"实例化 HttpFilter 错误：$clz 不是有效的 ${classOf[AbstractHttpFilter]}", e)
          None
      }
    } else {
      system.dynamicAccess.createInstanceFor[HttpFilter](clz, Nil) match {
        case Success(v) => Some(v)
        case Failure(e) =>
          logger.error(s"实例化 HttpFilter 错误：$clz 不是有效的 ${classOf[HttpFilter]}", e)
          None
      }
    }
  }

  private def generateConnectionContext(): ConnectionContext = {
    val http2 = httpSetting.http2
    if (!c.hasPath("ssl")) {
      HttpConnectionContext(http2)
    } else {
      val akkaSslConfig = new AkkaSSLConfig(system, httpSetting.createSSLConfig())
      val keyPassword   = c.getString("ssl.key-store.password")
      val keyPath       = c.getString("ssl.key-store.path")
      val keystore =
        Objects.requireNonNull(getClass.getClassLoader.getResourceAsStream(keyPath), s"keystore不能为空，keyPath: $keyPath")
      val keyStoreType = c.getOrElse("ssl.key-store.type", "PKCS12")
      val algorithm    = c.getOrElse("ssl.key-store.algorithm", "SunX509")
      val protocol     = c.getOrElse("ssl.protocol", akkaSslConfig.config.protocol)
      HttpUtils.generateHttps(keyPassword, keystore, keyStoreType, algorithm, protocol, http2, Some(akkaSslConfig))
    }
  }

  def isStarted(): Boolean = _isStarted.get()

  def isRunning(): Boolean = _isRunning

  private def _saveServer(socketAddress: InetSocketAddress): InetSocketAddress = {
    socketAddress.getAddress.getAddress.apply(0) == 0
    val inetAddress = socketAddress.getAddress match {
      case address if Objects.isNull(address) || address.getAddress.apply(0) == 0 =>
        NetworkUtils.firstOnlineInet4Address().getOrElse(socketAddress.getAddress)
      case address => address
    }
    val host = inetAddress.getHostAddress
    val port = socketAddress.getPort
    _socketAddress = new InetSocketAddress(inetAddress, port)
    System.setProperty(s"$id.server.host", host)
    System.setProperty(s"$id.server.port", port.toString)
    ConfigFactory.invalidateCaches()
    _socketAddress
  }

  private def afterHttpBindingSuccess(binding: ServerBinding, isSecure: Boolean): Unit = {
    val schema        = if (isSecure) "https" else "http"
    val socketAddress = _saveServer(binding.localAddress)
    logger.info(s"Server online at $schema://${socketAddress.getHostString}:${socketAddress.getPort}")
    _isRunning = true
    FusionCore(system).events.http.complete(HttpBindingServerEvent(Success(socketAddress), isSecure))
  }

  private def afterHttpBindingFailure(cause: Throwable, isSecure: Boolean): Unit = {
    val schema = if (isSecure) "https" else "http"
    logger.error(s"Error starting the $schema server ${cause.getMessage}", cause)
    close()
    FusionCore(system).events.http.complete(HttpBindingServerEvent(Failure(cause), isSecure))
  }

  /**
   * close后不能调用 [[startHandlerAsync()]]或[[startRouteAsync()]]重新启动，需要再次构造实例运行
   */
  override def close(): Unit = {
    maybeEventualBinding.foreach(_.foreach(_.unbind()))
    maybeEventualBinding = None
    _isStarted.set(false)
    _isRunning = false
    mat.shutdown()
  }

  def socketAddress: InetSocketAddress = _socketAddress
}
