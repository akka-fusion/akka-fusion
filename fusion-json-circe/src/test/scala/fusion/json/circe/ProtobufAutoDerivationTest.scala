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
