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

package fusion.mongodb.test

import akka.Done
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.Materializer
import akka.stream.Attributes
import akka.stream.Supervision
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.mongodb.client.model.Filters
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Polygon
import com.mongodb.client.model.geojson.Position
import com.mongodb.reactivestreams.client._
import fusion.data.mongodb.MongoTemplate
import helloscala.common.util.Utils
import org.bson.Document
import org.mongodb.scala.bson.conversions.Bson
import org.scalatest.FunSuiteLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class MongodbGeoTest extends ScalaTestWithActorTestKit with FunSuiteLike {
  private val IGNORE_KEYS = Set("location", "id")
  implicit private val classicSystem = system.toClassic
  implicit private val mat = Materializer(classicSystem)
  private val client = MongoClients.create("mongodb://123.206.9.104:27017")
  private val localClient = MongoClients.create("mongodb://localhost:27017")

  private val coll = client
    .getDatabase("fusion-resource_dev")
    .getCollection("school_3")
    .withCodecRegistry(MongoTemplate.DEFAULT_CODEC_REGISTRY)

  private val schoolPointColl = localClient
    .getDatabase("test")
    .getCollection("school_point")
    .withCodecRegistry(MongoTemplate.DEFAULT_CODEC_REGISTRY)

  test("migrate") {
    val insertManyF = Source
      .fromPublisher(coll.find())
      .addAttributes(Attributes(SupervisionStrategy(Supervision.resumingDecider)))
      .map { doc =>
        val Array(longitude, latitude) = doc.getString("location").split(',')
        val point = new Point(new Position(longitude.trim.toDouble, latitude.trim.toDouble))
        val data = doc.asScala.filter { case (key, _) => !IGNORE_KEYS(key) }.asJava
        new Document(data).append("location", point).append("_id", doc.getString("id"))
      }
      .via(Flow[Document].flatMapConcat(doc =>
        Source.fromPublisher(schoolPointColl.insertOne(doc)).map(_ => Some(doc)).recover { case _ => None }))
      .runWith(Sink.ignore)

    val (result, cost) = Utils.timing(Await.result(insertManyF, 30.minutes))
    result shouldBe Done
    println(s"cost time: $cost")
  }

  test("geo") {
    val filters = Filters.geoIntersects(
      "location",
      new Polygon(
        List(
          116.440476 -> 39.952362,
          116.490476 -> 39.952362,
          116.490476 -> 39.902362,
          116.420476 -> 39.902362,
          116.440476 -> 39.952362).map { case (left, right) => new Position(left, right) }.asJava))
    println(filters.toBsonDocument(classOf[Bson], schoolPointColl.getCodecRegistry).toJson())

    val list = Source.fromPublisher(schoolPointColl.find(filters)).runWith(Sink.seq).futureValue
    list should not be empty
    list.foreach(println)
    println(list.size)
  }
}
