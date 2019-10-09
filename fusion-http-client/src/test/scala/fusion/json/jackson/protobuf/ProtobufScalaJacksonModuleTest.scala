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

package fusion.json.jackson.protobuf

import com.google.protobuf.struct.Value
import fusion.ResultBO
import fusion.json.JsonUtils
import fusion.json.jackson.Jackson
import fusion.test.FusionTestFunSuite

class ProtobufScalaJacksonModuleTest extends FusionTestFunSuite {
  Jackson.defaultObjectMapper.registerModule(new ProtobufScalaJacksonModule)

  test("ProtobufScalaJacksonModuleTest") {
//    val json = Jackson.readTree(
//      """{"name":"羊八井","age":33,"n":null,"isfather":true,"double":89.3,"array":["aa","bb",null],"obj":{"width":3,"height":8,"other":null},"elements":[{"key":"key"},{"key":"yek","value":"value"}]}""")
    val json =
      Jackson.readTree("""{"name":"羊八井","age":33,"n":null,"isfather":true,"double":89.3}""")
    println("JsonNode: " + json)

    val value = Jackson.treeToValue[Value](json)
    println("Protobuf Value: " + value)

//    val json2 = Jackson.stringify(value)
//    println("String: " + json2)
  }

  test("ResultBO") {
    import JsonUtils.defaultFormats
    val bo = ResultBO()
    val text = JsonUtils.protobuf.toJsonString(bo)
    println(text)

    val jvalue = JsonUtils.parse(text)
    println(jvalue)

//    val v = jvalue.extract[ResultBO]
    val v = JsonUtils.protobuf.fromJson[ResultBO](jvalue)
    println(v)
  }

}
