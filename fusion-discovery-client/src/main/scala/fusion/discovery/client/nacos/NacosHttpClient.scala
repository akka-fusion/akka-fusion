package fusion.discovery.client.nacos

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.client.DiscoveryHttpClient
import fusion.discovery.client.DiscoveryHttpClientSetting
import fusion.discovery.client.FusionNamingService
import fusion.http.util.HttpUtils
import helloscala.common.util.Utils

import scala.concurrent.Future
import scala.reflect.ClassTag

@deprecated("推荐使用 DiscoveryHttpClient", "1.0.0")
final class NacosHttpClient private (
    val namingService: FusionNamingService,
    val clientSetting: DiscoveryHttpClientSetting)(implicit val system: ActorSystem)
    extends DiscoveryHttpClient
    with StrictLogging {

  def singleRequestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T] = {
    import fusion.http.util.JacksonSupport._
    singleRequest(req).flatMap(response => HttpUtils.mapHttpResponse(response))(materializer.executionContext)
  }

  def singleRequestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]] = {
    singleRequest(req).flatMap(response => HttpUtils.mapHttpResponseList(response))(materializer.executionContext)
  }

  override def buildUri(uri: Uri): Future[Uri] = Future {
    val host = uri.authority.host
    if (host.isNamedHost()) {
      val serviceName = host.address()
      val inst        = Utils.requireNonNull(namingService.selectOneHealthyInstance(serviceName), s"服务： $serviceName 不存在")
      val newUri      = uri.withAuthority(inst.ip, inst.port)
      logger.debug(s"build uri: $uri to $newUri")
      newUri
    } else {
      uri
    }
  }

}

object NacosHttpClient {

  def apply(namingService: FusionNamingService, clientSetting: DiscoveryHttpClientSetting)(
      implicit system: ActorSystem): NacosHttpClient =
    new NacosHttpClient(namingService, clientSetting)
}
