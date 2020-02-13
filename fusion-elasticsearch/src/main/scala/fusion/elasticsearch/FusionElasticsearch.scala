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

package fusion.elasticsearch

import akka.Done
import akka.actor.typed.ActorSystem
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http.JavaClient
import fusion.common.component.Components
import fusion.common.extension.{ FusionCoordinatedShutdown, FusionExtension, FusionExtensionId }
import fusion.core.extension.FusionCore
import helloscala.common.Configuration
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder

import scala.language.higherKinds
import scala.concurrent.Future

class FusionESClient(val underlying: ElasticClient, val config: Configuration) {
  def client: HttpClient = underlying.client

  /**
   * Returns a String containing the request details.
   * The string will have the HTTP method, endpoint, params and if applicable the request body.
   */
  def show[T](t: T)(implicit handler: Handler[T, _]): String = Show[ElasticRequest].show(handler.build(t))

  // Executes the given request type T, and returns an effect of Response[U]
  // where U is particular to the request type.
  // For example a search request will return a Response[SearchResponse].
  def execute[T, U, F[_]](t: T)(
      implicit
      executor: Executor[F],
      functor: Functor[F],
      handler: Handler[T, U]): F[Response[U]] = {
    val request = handler.build(t)
    val f = executor.exec(client, request)
    functor.map(f) { resp =>
      handler.responseHandler.handle(resp) match {
        case Right(u)    => RequestSuccess(resp.statusCode, resp.entity.map(_.content), resp.headers, u)
        case Left(error) => RequestFailure(resp.statusCode, resp.entity.map(_.content), resp.headers, error)
      }
    }
  }

  def close(): Unit = client.close()
}

class ElasticsearchComponents(system: ActorSystem[_])
    extends Components[FusionESClient]("fusion.elasticsearch.default") {
  override def configuration: Configuration = FusionCore(system).configuration

  override protected def createComponent(id: String): FusionESClient = {
    val c = configuration.getConfiguration(id)
    val props = ElasticProperties(c.getString("uri"))
    val client = JavaClient(props, (requestConfigBuilder: RequestConfig.Builder) => {
      c.get[Option[Configuration]]("request-config").foreach(customizeRequestConfigFunc(_, requestConfigBuilder))
      requestConfigBuilder
    }, (httpClientBuilder: HttpAsyncClientBuilder) => {
      c.get[Option[Configuration]]("http-config").foreach(customizeHttpClientFunc(_, httpClientBuilder))
      httpClientBuilder
    })
    new FusionESClient(ElasticClient(client), c)
  }

  private def customizeRequestConfigFunc(c: Configuration, b: RequestConfig.Builder): RequestConfig.Builder = {
    c.get[Option[Boolean]]("authenticationEnabled").foreach(b.setAuthenticationEnabled)
    b
  }

  private def customizeHttpClientFunc(c: Configuration, b: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
    b
  }

  override protected def componentClose(c: FusionESClient): Future[Done] =
    Future {
      c.close()
      Done
    }(system.executionContext)
}

class FusionElasticsearch private (override val system: ActorSystem[_]) extends FusionExtension {
  val components = new ElasticsearchComponents(system)
  FusionCoordinatedShutdown(system).beforeActorSystemTerminate("StopFusionElasticsearch") { () =>
    components.closeAsync()(system.executionContext)
  }
  def component: FusionESClient = components.component
}

object FusionElasticsearch extends FusionExtensionId[FusionElasticsearch] {
  override def createExtension(system: ActorSystem[_]): FusionElasticsearch = new FusionElasticsearch(system)
}
