package fusion.mongodb.test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.mongodb.{ConnectionString, MongoClientSettings}
import fusion.data.mongodb.MongoTemplate
import fusion.test.FusionTestFunSuite
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.time.{Millis, Span}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class FileEntity(_id: String, fileName: String, size: Long, localPath: String, hash: String)

class MongodbTest extends FusionTestFunSuite with BeforeAndAfterAll {

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(15, Millis)))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val mongoClientSettings = MongoClientSettings
    .builder()
    .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
    .codecRegistry(DEFAULT_CODEC_REGISTRY)
    .build()
  val client = MongoTemplate(MongoClient(mongoClientSettings))

  val fileCollection: MongoCollection[FileEntity] =
    client.getCollection[FileEntity]("abc", "file", List(classOf[FileEntity]))

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

    val result = fileCollection.insertOne(data.head).toFuture().futureValue
    println(result)

//    val docs: immutable.Seq[FileEntity] = MongoSource[FileEntity](fileCollection.find()).runWith(Sink.seq).futureValue
//    docs must not be empty
//    docs.foreach(println)
  }

  override protected def afterAll(): Unit = {
    val f = client.getDatabase("abc").getCollection("file").drop().toFuture()
    Await.ready(f, Duration.Inf)
    client.close()
    system.terminate()
  }

}
