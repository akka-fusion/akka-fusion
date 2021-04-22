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

///*
// * Copyright 2019 helloscala.com
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fusion.json.jackson.deser
//
//import com.fasterxml.jackson.core.{ JsonParseException, JsonParser }
//import com.fasterxml.jackson.databind.{ BeanProperty, DeserializationContext, JsonDeserializer }
//import com.fasterxml.jackson.databind.deser.ContextualDeserializer
//import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
//import com.typesafe.scalalogging.StrictLogging
//import helloscala.common.util.{ EnumTrait, EnumTraitCompanion }
//
//class CustomEnumTraitSerializer(enumClass: Class[EnumTrait])
//    extends StdScalarDeserializer[EnumTrait](enumClass)
//    with ContextualDeserializer
//    with StrictLogging {
//  override def deserialize(p: JsonParser, ctxt: DeserializationContext): EnumTrait = {
//    val value = p.getValueAsInt
//    val method = enumClass.getMethod("companion")
//    val companion = method.invoke(enumClass).asInstanceOf[EnumTraitCompanion]
//    val option = classOf[EnumTraitCompanion].getMethod("fromValue", classOf[Int]).invoke(companion, Int.box(value))
//    option.asInstanceOf[Option[_]] match {
//      case Some(value) => value.asInstanceOf[EnumTrait]
//      case None        => throw new JsonParseException(p, "EnumTrait serializer error.")
//    }
//  }
//
//  override def createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer[_] = this
//}
