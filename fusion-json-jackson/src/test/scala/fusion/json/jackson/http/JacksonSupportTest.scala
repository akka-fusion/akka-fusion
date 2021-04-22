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

package fusion.json.jackson.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import akka.http.scaladsl.unmarshalling.{ Unmarshal, Unmarshaller }
import akka.stream.scaladsl.{ Sink, Source }
import com.fasterxml.jackson.databind.ObjectMapper
import fusion.json.jackson.JacksonObjectMapperExtension
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

final case class Foo(bar: String) {
  require(bar.startsWith("bar"), "bar must start with 'bar'!")
}

final case class FinReq(id: Long, detail: List[Foo])

class JacksonSupportTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  implicit private val system = ActorSystem()
  private val jacksonSupport = JacksonObjectMapperExtension(system).jacksonSupport
  private val objectMapperJson = JacksonObjectMapperExtension(system).objectMapperJson
  import jacksonSupport._

  "JacksonSupport" should {
    "test" in {
      Future {
        val jsonstr = """{"id":23,"detail":[{"bar":"bar哈哈哈"}]}"""
        val bean = objectMapperJson.readValue[FinReq](jsonstr)
        println(bean)
        bean.detail.head shouldBe Foo("bar哈哈哈")
        objectMapperJson.stringify(bean) shouldBe jsonstr
      }
    }

    "should enable marshalling and unmarshalling of case classes" in {
      val foo = Foo("bar")
      Marshal(foo).to[RequestEntity].flatMap(Unmarshal(_).to[Foo]).map(_ shouldBe foo)
    }

    "enable streamed marshalling and unmarshalling for json arrays" in {
      val foos = (0 to 100).map(i => Foo(s"bar-$i")).toList

      Marshal(Source(foos))
        .to[RequestEntity]
        .flatMap { entity =>
          println(entity)
          Unmarshal(entity).to[SourceOf[Foo]]
        }
        .flatMap(_.runWith(Sink.seq))
        .map(_ shouldBe foos)
    }

    "enable plain marshalling and unmarshalling for json arrays" in {
      val foos = (0 to 100).map(i => Foo(s"bar-$i")).toList

      Marshal(foos).to[RequestEntity].flatMap(entity => Unmarshal(entity).to[List[Foo]]).map(_ shouldBe foos)
    }

    "should enable marshalling and unmarshalling of arrays of values" in {
      val foo = Seq(Foo("bar"))
      Marshal(foo).to[RequestEntity].flatMap(Unmarshal(_).to[Seq[Foo]]).map(_ shouldBe foo)
    }

    "provide proper error messages for requirement errors" in {
      val entity = HttpEntity(MediaTypes.`application/json`, """{ "bar": "baz" }""")
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(_.getMessage should include("requirement failed: bar must start with 'bar'!"))
    }

    "fail with NoContentException when unmarshalling empty entities" in {
      val entity = HttpEntity.empty(MediaTypes.`application/json`)
      Unmarshal(entity).to[Foo].failed.map(_ shouldBe Unmarshaller.NoContentException)
    }

    "fail with UnsupportedContentTypeException when Content-Type is not `application/json`" in {
      val entity = HttpEntity("""{ "bar": "bar" }""")
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(
          _ shouldBe UnsupportedContentTypeException(
            Some(ContentTypes.`text/plain(UTF-8)`),
            MediaTypes.`application/json`))
    }

    "allow unmarshalling with passed in Content-Types" in {
      val foo = Foo("bar")
      val `application/json-home` =
        MediaType.applicationWithFixedCharset("json-home", HttpCharsets.`UTF-8`, "json-home")

      final object CustomJacksonSupport extends JacksonSupport {
        implicit override val objectMapper: ObjectMapper = JacksonObjectMapperExtension(system).objectMapperJson

        override val unmarshallerContentTypes = List(MediaTypes.`application/json`, `application/json-home`)
      }
      import CustomJacksonSupport._

      val entity = HttpEntity(`application/json-home`, """{ "bar": "bar" }""")
      Unmarshal(entity).to[Foo].map(_ shouldBe foo)
    }
  }

  override protected def afterAll() = {
    Await.ready(system.terminate(), 42.seconds)
    super.afterAll()
  }
}
