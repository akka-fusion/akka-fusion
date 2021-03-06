/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.json.jackson

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ExtendedActorSystem
import akka.serialization.jackson.JacksonObjectMapperProvider
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import fusion.common.extension.{ FusionExtension, FusionExtensionId }
import fusion.json.jackson.http.{ JacksonSupport, JacksonSupportImpl }

class JacksonObjectMapperExtension private (override val classicSystem: ExtendedActorSystem) extends FusionExtension {
  private val objectMappers = new ConcurrentHashMap[String, ScalaObjectMapper]()

  val objectMapperJson: ScalaObjectMapper = getOrCreate("jackson-json", None)

  val jacksonSupport: JacksonSupport = new JacksonSupportImpl()(objectMapperJson)

  lazy val objectMapperCbor: ScalaObjectMapper = getOrCreate("jackson-cbor", Some(new CBORFactory))

  private[fusion] def getOrCreate(bindingName: String, jsonFactory: Option[JsonFactory]): ScalaObjectMapper =
    objectMappers.computeIfAbsent(
      bindingName,
      _ => new ScalaObjectMapper(JacksonObjectMapperProvider(classicSystem).getOrCreate(bindingName, jsonFactory)))
}

object JacksonObjectMapperExtension extends FusionExtensionId[JacksonObjectMapperExtension] {

  override def createExtension(system: ExtendedActorSystem): JacksonObjectMapperExtension =
    new JacksonObjectMapperExtension(system)
}
