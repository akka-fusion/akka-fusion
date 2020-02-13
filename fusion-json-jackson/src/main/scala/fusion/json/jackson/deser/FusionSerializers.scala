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

//package fusion.json.jackson.deser
//
//import com.fasterxml.jackson.databind.{ BeanDescription, JavaType, JsonSerializer, SerializationConfig }
//import com.fasterxml.jackson.databind.ser.Serializers
//import helloscala.common.util.EnumTrait
//
//class FusionSerializers extends Serializers.Base {
//  override def findSerializer(
//      config: SerializationConfig,
//      `type`: JavaType,
//      beanDesc: BeanDescription): JsonSerializer[_] = {
//    if (classOf[EnumTrait].isAssignableFrom(`type`.getRawClass)) {
//      new CustomEnumTraitSerializer(`type`.getRawClass.asInstanceOf[Class[EnumTrait]])
//    } else {
//      super.findSerializer(config, `type`, beanDesc)
//    }
//  }
//}
