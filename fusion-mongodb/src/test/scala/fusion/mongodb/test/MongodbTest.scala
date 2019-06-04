package fusion.mongodb.test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import fusion.data.mongodb.MongoTemplate
import fusion.test.FusionTestFunSuite
import org.bson.Document
import org.bson.types.ObjectId
import org.scalatest.BeforeAndAfterAll
import org.scalatest.time.Millis
import org.scalatest.time.Span

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.mongodb.scala.bson.codecs.Macros._

case class FileEntity(_id: String, fileName: String, size: Long, localPath: String, hash: String)

class MongodbTest extends FusionTestFunSuite with BeforeAndAfterAll {

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(15, Millis)))

  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()

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
    val docs = MongoSource[FileEntity](fileCollection.find()).runWith(Sink.seq).futureValue
    docs must not be empty
    docs.foreach(println)
  }

  private val client   = MongoClients.create()
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
