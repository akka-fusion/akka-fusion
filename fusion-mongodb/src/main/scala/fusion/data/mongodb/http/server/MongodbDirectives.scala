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

package fusion.data.mongodb.http.server

import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import org.bson.types.ObjectId

trait MongodbDirectives {
  implicit def objectIdFromStringUnmarshaller: FromStringUnmarshaller[ObjectId] =
    MongodbDirectives._objectIdFromStringUnmarshaller
}

object MongodbDirectives extends MongodbDirectives {
  private val _objectIdFromStringUnmarshaller = Unmarshaller.strict[String, ObjectId](new ObjectId(_))
}
