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

package fusion.json.jackson.deser

import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.util.EnumResolver
import com.fasterxml.jackson.databind.{ BeanDescription, DeserializationConfig, JsonDeserializer }

class FusionDeserializers extends Deserializers.Base {

  override def findEnumDeserializer(
      `type`: Class[_],
      config: DeserializationConfig,
      beanDesc: BeanDescription): JsonDeserializer[_] = {
    if (classOf[Enum[_]].isAssignableFrom(`type`)) {
      new CustomEnumDeserializer(EnumResolver.constructUnsafe(`type`, config.getAnnotationIntrospector))
//    } else if (classOf[EnumTrait].isAssignableFrom(`type`)) {
//      new CustomEnumTraitSerializer(`type`.asInstanceOf[Class[EnumTrait]])
    } else {
      super.findEnumDeserializer(`type`, config, beanDesc)
    }
  }
}
