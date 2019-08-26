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

package fusion.json.circe

import com.google.protobuf.struct.Value
import fusion.json.circe.ProtobufAutoDerivation._
import io.circe.syntax._
import fusion.test.FusionTestFunSuite
import io.circe.parser.parse

class ProtobufAutoDerivationTest extends FusionTestFunSuite {
  test("ProtobufAutoDerivationTest") {
    val json = parse(
      """{"name":"羊八井","age":33,"n":null,"isfather":true,"double":89.3,"array":["aa","bb",null],"obj":{"width":3,"height":8,"other":null},"elements":[{"key":"key"},{"key":"yek","value":"value"}]}""").right.value
    println(json.noSpaces)

    val valueEither = json.as[Value]
    println(valueEither)
    val value = valueEither.right.value
    println(value)

    println(value.asJson.noSpaces)
  }

}
