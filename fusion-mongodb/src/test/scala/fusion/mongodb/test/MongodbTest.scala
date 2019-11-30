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

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import fusion.data.mongodb.MongoTemplate
import org.bson.Document
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.Macros._
import org.scalatest.FunSuiteLike

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class FileEntity(_id: String, fileName: String, size: Long, localPath: String, hash: String)

class MongodbTest extends ScalaTestWithActorTestKit with FunSuiteLike {
  implicit val classicSystem = system.toClassic
  implicit val materializer = Materializer(classicSystem)

  val mongoClientSettings = MongoClientSettings
    .builder()
    .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
    .codecRegistry(MongoTemplate.DEFAULT_CODEC_REGISTRY)
    .build()
  val template = MongoTemplate(MongoClients.create(mongoClientSettings))

  val fileCollection: MongoCollection[FileEntity] =
    template.getDatabaseCollection("abc", "file", List(classOf[FileEntity]))

  test("mongodb") {
    val data = List(
      FileEntity("3", "name.txt", 23432L, "/tmp/name.txt", "23423423423423423423423432")
////      FileEntity("33423423423423423423423432",
////                 "name.txt",
////                 23432L,
////                 "", //Paths.get("/tmp/name.txt"),
////                 "23423423423423423423423432"),
////      FileEntity("4", "name.txt", 23432L, "" /*Paths.get("/tmp/name.txt")*/, "23423423423423423423423432")
    )
//    val result: Done = Source(data)
//      .grouped(10)
//      .runWith(MongoSink.insertMany(2, fileCollection))
//      .futureValue
//    println(result)

    val result = Source.fromPublisher(fileCollection.insertOne(data.head)).runWith(Sink.head).futureValue
    println(result)
  }

  test("find") {
    val docs = Source.fromPublisher(fileCollection.find()).runWith(Sink.seq).futureValue
    docs should not be empty
    docs.foreach(println)
  }

  private val client = MongoClients.create()
  private val testColl = client.getDatabase("test").getCollection("test")

  test("insert duplicate") {
    val ret = Source
      .fromPublisher(
        testColl.insertOne(new Document("_id", new ObjectId("5c77d4c5f86831232761e850")).append("name", "杨景")))
      .runWith(Sink.head)
      .futureValue
    println(ret)
    val ret2 = Source
      .fromPublisher(
        testColl.insertOne(new Document("_id", new ObjectId("5c77d4c5f86831232761e850")).append("name", "杨景")))
      .runWith(Sink.head)
      .futureValue
    println(ret2)
  }

  test("update upsert") {
    val id = "5c77d4c5f86831232761e890"
    val ret = Source
      .fromPublisher(
        testColl.replaceOne(Filters.eq("_id", id), new Document("name", "杨景"), new ReplaceOptions().upsert(true)))
      .runWith(Sink.head)
      .futureValue
    println(ret)
    val ret2 = Source
      .fromPublisher(testColl.replaceOne(Filters.eq("_id", id), new Document("name", "杨景").append("age", 33)))
      .runWith(Sink.head)
      .futureValue
    println(ret2)
  }

  test("geo") {}

  override protected def afterAll(): Unit = {
    val f = Source.fromPublisher(template.getDatabase("abc").getCollection("file").drop()).runWith(Sink.head)
    Await.ready(f, Duration.Inf)
    client.close()
    template.close()
    system.terminate()
  }
}
