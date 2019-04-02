package fusion.mongodb.test

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.ActorMaterializer
import akka.stream.Attributes
import akka.stream.Supervision
import com.mongodb.client.model.Filters
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Polygon
import com.mongodb.client.model.geojson.Position
import com.mongodb.reactivestreams.client._
import fusion.data.mongodb.MongoTemplate
import fusion.test.FusionTestFunSuite
import helloscala.common.util.Utils
import org.bson.Document
import org.mongodb.scala.bson.conversions.Bson
import org.scalatest.BeforeAndAfterAll

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal

class MongodbGeoTest extends FusionTestFunSuite with BeforeAndAfterAll {
  private val IGNORE_KEYS     = Set("location", "id")
  implicit private val system = ActorSystem("test")
  implicit private val mat    = ActorMaterializer()
  private val client          = MongoClients.create("mongodb://123.206.9.104:27017")
  private val localClient     = MongoClients.create("mongodb://localhost:27017")
  private val coll = client
    .getDatabase("hongka-resource_dev")
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
        val point                      = new Point(new Position(longitude.trim.toDouble, latitude.trim.toDouble))
        new Document(doc.asScala.filterKeys(key => !IGNORE_KEYS(key)).asJava)
          .append("location", point)
          .append("_id", doc.getString("id"))
      }
      .via(Flow[Document].flatMapConcat(doc =>
        Source.fromPublisher(schoolPointColl.insertOne(doc)).map(_ => Some(doc)).recover { case NonFatal(e) => None }))
      .runWith(Sink.ignore)

    val (result, cost) = Utils.timing(Await.result(insertManyF, 30.minutes))
    result mustBe Done
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
    list must not be empty
    list.foreach(println)
    println(list.size)
  }

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 10.seconds)
    super.afterAll()
  }
}
