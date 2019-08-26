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

package fusion.http.util

import akka.http.scaladsl.server.Rejection
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable

final class DefaultRejectionHandler extends RejectionHandler with StrictLogging {
  override def apply(rejections: immutable.Seq[Rejection]): Option[Route] = {
    BaseRejectionBuilder.rejectionHandler(rejections)
  }
}
