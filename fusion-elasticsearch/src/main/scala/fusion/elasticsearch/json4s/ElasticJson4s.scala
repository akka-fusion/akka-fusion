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

package fusion.elasticsearch.json4s

import com.sksamuel.elastic4s.AggReader
import com.sksamuel.elastic4s.Hit
import com.sksamuel.elastic4s.HitReader
import com.sksamuel.elastic4s.Indexable
import com.sksamuel.exts.Logging
import fusion.json.JsonUtils
import org.json4s.Formats
import org.json4s.Serialization

import scala.util.Try

object ElasticJson4s {
  object Implicits extends Logging {
    implicit def Json4sHitReader[T](
        implicit mf: Manifest[T],
        formats: Formats = JsonUtils.defaultFormats,
        json4s: Serialization = JsonUtils.serialization): HitReader[T] =
      (hit: Hit) =>
        Try {
          json4s.read[T](hit.sourceAsString)
        }

    implicit def Json4sAggReader[T](
        implicit mf: Manifest[T],
        formats: Formats = JsonUtils.defaultFormats,
        json4s: Serialization = JsonUtils.serialization): AggReader[T] =
      (json: String) =>
        Try {
          json4s.read[T](json)
        }

    implicit def Json4sIndexable[T <: AnyRef](
        implicit formats: Formats = JsonUtils.defaultFormats,
        json4s: Serialization = JsonUtils.serialization): Indexable[T] =
      (t: T) => json4s.write(t)
  }
}
