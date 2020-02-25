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

package fusion.elasticsearch.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.sksamuel.elastic4s.{ Hit, HitReader, Indexable }
import com.sksamuel.exts.Logging
import fusion.json.jackson.Jackson

import scala.util.Try

object ElasticJackson {
  object Implicits extends Logging {
    implicit def JacksonJsonIndexable[T](implicit mapper: ObjectMapper = Jackson.defaultObjectMapper): Indexable[T] =
      (t: T) => mapper.writeValueAsString(t)

    implicit def JacksonJsonHitReader[T](
        implicit manifest: Manifest[T],
        mapper: ObjectMapper = Jackson.defaultObjectMapper): HitReader[T] =
      (hit: Hit) =>
        Try {
          require(hit.sourceAsString != null)
          val node = mapper.readTree(hit.sourceAsString).asInstanceOf[ObjectNode]
          if (!node.has("_id")) node.put("_id", hit.id)
          if (!node.has("_type")) node.put("_type", hit.`type`)
          if (!node.has("_index")) node.put("_index", hit.index)
          //  if (!node.has("_score")) node.put("_score", hit.score)
          if (!node.has("_version")) node.put("_version", hit.version)
          if (!node.has("_timestamp"))
            hit
              .sourceFieldOpt("_timestamp")
              .collect {
                case f => f.toString
              }
              .foreach(node.put("_timestamp", _))
          mapper.readValue(mapper.writeValueAsBytes(node), manifest.runtimeClass).asInstanceOf[T]
        }
  }
}
