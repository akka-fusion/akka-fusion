package fusion.mongodb.test

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mongodb.scaladsl.{MongoSink, MongoSource}
import akka.stream.scaladsl.{Sink, Source}
import fusion.test.FusionTestFunSuite
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala.{Document, MongoClient, MongoCollection}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.time.{Millis, Span}

import scala.collection.immutable

case class FileEntity(_id: String, fileName: String, size: Long, localPath: String, hash: String)

class MongodbTest extends FusionTestFunSuite with BeforeAndAfterAll {

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(15, Millis)))
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val client = MongoClient("mongodb://localhost:27017")
  val db = client.getDatabase("hongka-resource")

  val codeRegistry =
    CodecRegistries.fromRegistries(CodecRegistries.fromProviders(classOf[FileEntity]), DEFAULT_CODEC_REGISTRY)
  val fileCollection: MongoCollection[FileEntity] = db.getCollection[FileEntity]("file").withCodecRegistry(codeRegistry)

  test("mongodb") {
    val source = Source(
      List(
        FileEntity("3", "name.txt", 23432L, "/tmp/name.txt", "23423423423423423423423432"),
        FileEntity("33423423423423423423423432", "name.txt", 23432L, "/tmp/name.txt", "23423423423423423423423432"),
        FileEntity("4", "name.txt", 23432L, "/tmp/name.txt", "23423423423423423423423432")
      ))
    val result: Done = source.grouped(10).runWith(MongoSink.insertMany(1, fileCollection)).futureValue
    println(result)

    val docs: immutable.Seq[FileEntity] = MongoSource[FileEntity](fileCollection.find()).runWith(Sink.seq).futureValue
    println(docs)
  }

  override protected def afterAll(): Unit = {
    client.close()
    system.terminate()
  }

}
