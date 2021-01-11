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

package fusion.cassandra.`type`.codec

import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeParameter
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken
import edu.umd.cs.findbugs.annotations.NonNull

class ScalaTypeParameter[T] extends TypeParameter[T]() {}

object ScalaGenericType {

  @NonNull def seqOf[T](@NonNull elementType: GenericType[T]): GenericType[Seq[T]] = {
    val token: TypeToken[Seq[T]] =
      new TypeToken[Seq[T]]() {}.where(new ScalaTypeParameter[T](), elementType.__getToken())
    GenericType.of(token.getType).asInstanceOf[GenericType[Seq[T]]]
  }
}
