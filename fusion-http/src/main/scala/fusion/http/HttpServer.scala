/*
 * Copyright 2019 akka-fusion.com
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

package fusion.http

import java.net.InetSocketAddress
import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean

import akka.Done
import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorSystem
import akka.http.FusionRoute
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, Uri }
import akka.http.scaladsl.server.{ ExceptionHandler, RejectionHandler, Route }
import akka.http.scaladsl.{ ConnectionContext, Http, HttpConnectionContext }
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.core.event.http.HttpBindingServerEvent
import fusion.core.extension.FusionCore
import fusion.core.http.HttpHandler
import fusion.http.constant.HttpConstants
import fusion.http.interceptor.HttpInterceptor
import fusion.http.server.BaseRoute
import fusion.http.util.HttpUtils
import helloscala.common.Configuration
import helloscala.common.exception.HSInternalErrorException
import helloscala.common.util.NetworkUtils

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContextExecutor, Future }
import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }

final class HttpServer(val id: String, implicit val system: ExtendedActorSystem)
    extends StrictLogging
    with AutoCloseable {
  implicit private val mat: Materializer = Materializer.matFromSystem(system)
  implicit private def ec: ExecutionContextExecutor = mat.executionContext
  private val _isStarted = new AtomicBoolean(false)
  @volatile private var _isRunning = false
  private def dynamicAccess = system.dynamicAccess

  private val c = Configuration(
    system.settings.config.getConfig(id).withFallback(system.settings.config.getConfig("fusion.default.http")))
  private var _schema: String = _
  private var _socketAddress: InetSocketAddress = _
  private var maybeEventualBinding = Option.empty[Future[ServerBinding]]
  private val httpSetting = HttpSetting(c, system.settings.config)
  logger.debug("httpSetting: " + httpSetting)

  @throws(classOf[Exception])
  def startHandlerSync(handler: HttpHandler)(
      implicit
      rejectionHandler: RejectionHandler = createRejectionHandler(),
      exceptionHandler: ExceptionHandler = createExceptionHandler(),
      duration: Duration = 30.seconds): ServerBinding =
    Await.result(startHandlerAsync(handler), duration)

  def startHandlerAsync(handler: HttpHandler)(
      implicit rejectionHandler: RejectionHandler = createRejectionHandler(),
      exceptionHandler: ExceptionHandler = createExceptionHandler()): Future[ServerBinding] = {
    import akka.http.scaladsl.server.Directives._
    val route = extractRequest { request =>
      complete(handler(request))
    }
    startRouteAsync(route)
  }

  def startBaseRouteSync(route: BaseRoute)(
      implicit
      rejectionHandler: RejectionHandler = createRejectionHandler(),
      exceptionHandler: ExceptionHandler = createExceptionHandler(),
      duration: Duration = 30.seconds): ServerBinding = {
    Await.result(startRouteAsync(route.route), duration)
  }

  @throws(classOf[Exception])
  def startRouteSync(route: Route)(
      implicit
      rejectionHandler: RejectionHandler = createRejectionHandler(),
      exceptionHandler: ExceptionHandler = createExceptionHandler(),
      duration: Duration = 30.seconds): ServerBinding = {
    Await.result(startRouteAsync(route), duration)
  }

  def startRouteAsync(_route: Route)(
      implicit rejectionHandler: RejectionHandler = createRejectionHandler(),
      exceptionHandler: ExceptionHandler = createExceptionHandler()): Future[ServerBinding] = {
    if (!_isStarted.compareAndSet(false, true)) {
      throw HSInternalErrorException("HttpServer只允许start一次")
    }

    val connectionContext = generateConnectionContext()

    val handler = toHandler(Route.seal(_route))
    val bindingFuture =
      Http().bindAndHandleAsync(handler, httpSetting.server.host, httpSetting.server.port, connectionContext)
    maybeEventualBinding = Some(bindingFuture)
    bindingFuture.failed.foreach { cause =>
      afterHttpBindingFailure(cause, connectionContext.isSecure)
    }
    bindingFuture.map { binding =>
      afterHttpBindingSuccess(binding, connectionContext.isSecure)
      binding
    }
  }

  private def toHandler(_route: Route): HttpRequest => Future[HttpResponse] = {
    val route =
      (getDefaultInterceptor() ++ getHttpInterceptors()).reverse.foldLeft(_route)((r, i) => i.interceptor(r))
    FusionRoute.asyncHandler(route)
  }

  private def getDefaultInterceptor(): Seq[HttpInterceptor] =
    httpSetting.defaultInterceptor.flatMap(createHttpInterceptor)

  private def getHttpInterceptors(): Seq[HttpInterceptor] = {
    httpSetting.httpInterceptors.flatMap(className => createHttpInterceptor(className))
  }

  private def createRejectionHandler(): RejectionHandler = {
    val clz = Class.forName(httpSetting.rejectionHandler)
    createInstanceFor[RejectionHandler](clz) match {
      case Success(rejectionHandler) => rejectionHandler
      case Failure(e) =>
        throw new ExceptionInInitializerError(
          s"$clz 不是有效的 akka.http.scaladsl.server.RejectionHandler，${e.getLocalizedMessage}")
    }
  }

  private def createExceptionHandler(): ExceptionHandler = {
    val clz = Class.forName(httpSetting.exceptionHandler)
    createInstanceFor[ExceptionHandler.PF](clz) match {
      case Success(pf) => ExceptionHandler(pf)
      case Failure(e) =>
        throw new ExceptionInInitializerError(
          s"$clz 不是有效的 akka.http.scaladsl.server.ExceptionHandler.PF，${e.getLocalizedMessage}")
    }
  }

  private def createHttpInterceptor(className: String): Option[HttpInterceptor] = {
    val clz = Class.forName(className)
    createInstanceFor[HttpInterceptor](clz) match {
      case Success(v) => Some(v)
      case Failure(e) =>
        logger.error(s"实例化 HttpFilter 错误：$clz 不是有效的 ${classOf[HttpInterceptor]}", e)
        None
    }
  }

  private def generateConnectionContext(): ConnectionContext = {
    if (!c.hasPath("ssl")) {
      HttpConnectionContext()
    } else {
      val akkaSslConfig = httpSetting.createSSLConfig()
      val keyPassword = c.getString("ssl.key-store.password")
      val keyPath = c.getString("ssl.key-store.path")
      val keystore =
        Objects.requireNonNull(getClass.getClassLoader.getResourceAsStream(keyPath), s"keystore不能为空，keyPath: $keyPath")
      val keyStoreType = c.getOrElse("ssl.key-store.type", "PKCS12")
      val algorithm = c.getOrElse("ssl.key-store.algorithm", "SunX509")
      val protocol = c.getOrElse("ssl.protocol", akkaSslConfig.protocol)
      HttpUtils.generateHttps(keyPassword, keystore, keyStoreType, algorithm, protocol)
    }
  }

  def isStarted: Boolean = _isStarted.get()

  def isRunning: Boolean = _isRunning

  def whenBinding: Option[Future[ServerBinding]] = maybeEventualBinding

  private def _saveServer(socketAddress: InetSocketAddress): InetSocketAddress = {
    val inetAddress = socketAddress.getAddress match {
      case address if Objects.isNull(address) || address.getAddress.apply(0) == 0 =>
        logger.info(s"绑定地址为所有地址：$address，尝试查找本机第一个有效网络地址")
        NetworkUtils
          .firstOnlineInet4Address()
          .map { add =>
            logger.info(s"找到本机第一个有效网络地址：$add")
            add
          }
          .getOrElse(socketAddress.getAddress)
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
    _schema = if (isSecure) "https" else "http"
    val socketAddress = _saveServer(binding.localAddress)
    logger.info(s"Server online at $schema://${socketAddress.getHostString}:${socketAddress.getPort}")
    _isRunning = true
    if (id == HttpConstants.PATH_DEFAULT) {
      FusionCore(system).events.http.complete(HttpBindingServerEvent(Success(socketAddress), isSecure))
    }
  }

  private def afterHttpBindingFailure(cause: Throwable, isSecure: Boolean): Unit = {
    val schema = if (isSecure) "https" else "http"
    logger.error(s"Error starting the $schema server ${cause.getMessage}", cause)
    close()
    if (id == HttpConstants.PATH_DEFAULT) {
      FusionCore(system).events.http.complete(HttpBindingServerEvent(Failure(cause), isSecure))
    }
  }

  /**
   * close后不能调用 [[startHandlerAsync()]]或[[startRouteAsync()]]重新启动，需要再次构造实例运行
   */
  override def close(): Unit = {
    closeAsync()
  }

  def closeAsync(): Future[Done] = {
    val future = maybeEventualBinding match {
      case Some(binding) => binding.flatMap(_.unbind())
      case _             => Future.successful(Done)
    }
    future.map { done =>
      maybeEventualBinding = None
      _isStarted.set(false)
      _isRunning = false
      done
    }
  }

  def socketAddress: InetSocketAddress = _socketAddress

  def schema: String = _schema

  @inline def buildUri(path: String): Uri = buildUri(Uri.Path(path), Uri.Query())

  def buildUri(path: Uri.Path, query: Uri.Query = Uri.Query()): Uri = {
    val sa = socketAddress
    Uri(schema, authority = Uri.Authority(Uri.Host(sa.getAddress.getHostAddress), sa.getPort), path).withQuery(query)
  }

  private def createInstanceFor[T: ClassTag](clazz: Class[_]): Try[T] = {
    import akka.actor.typed.scaladsl.adapter._
    dynamicAccess
      .createInstanceFor[T](clazz, List(classOf[ExtendedActorSystem] -> system))
      .orElse(dynamicAccess.createInstanceFor[T](clazz, List(classOf[ActorSystem[Nothing]] -> system.toTyped)))
      //      .orElse(dynamicAccess.createInstanceFor[T](clazz, List(classOf[classic.ActorSystem] -> system)))
      .orElse(dynamicAccess.createInstanceFor[T](clazz, Nil))
  }
}
