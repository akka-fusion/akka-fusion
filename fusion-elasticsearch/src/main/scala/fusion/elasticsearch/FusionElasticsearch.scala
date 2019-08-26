/*
 * Copyright 2019 helloscala.com
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
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticProperties
import com.sksamuel.elastic4s.http.HttpClient
import fusion.core.component.Components
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import helloscala.common.Configuration
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback

import scala.concurrent.Future

class FusionESClient(val underlying: ElasticClient, val config: Configuration) extends ElasticClient {
  override def client: HttpClient = underlying.client

  override def close(): Unit = underlying.close()
}

class ElasticsearchComponents(system: ExtendedActorSystem)
    extends Components[FusionESClient]("fusion.elasticsearch.default") {
  override def configuration: Configuration = FusionCore(system).configuration

  override protected def createComponent(id: String): FusionESClient = {
    val c = configuration.getConfiguration(id)
    val props = ElasticProperties(c.getString("uri"))
    val client = ElasticClient(
      props,
      new RequestConfigCallback {
        override def customizeRequestConfig(requestConfigBuilder: RequestConfig.Builder): RequestConfig.Builder = {
          c.get[Option[Configuration]]("request-config").foreach(customizeRequestConfigFunc(_, requestConfigBuilder))
          requestConfigBuilder
        }
      },
      new HttpClientConfigCallback {
        override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
          c.get[Option[Configuration]]("http-config").foreach(customizeHttpClientFunc(_, httpClientBuilder))
          httpClientBuilder
        }
      })
    new FusionESClient(client, c)
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
    }(system.dispatcher)
}

class FusionElasticsearch private (override protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new ElasticsearchComponents(_system)
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionElasticsearch") { () =>
    components.closeAsync()(system.dispatcher)
  }
  def component: FusionESClient = components.component
}

object FusionElasticsearch extends ExtensionId[FusionElasticsearch] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionElasticsearch = new FusionElasticsearch(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionElasticsearch
}
