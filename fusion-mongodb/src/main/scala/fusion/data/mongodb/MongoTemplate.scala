package fusion.data.mongodb

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.mongodb.ClientSessionOptions
import com.mongodb.reactivestreams.client._
import fusion.data.mongodb.codec.FusionCodecProvider
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.mongodb.scala.bson.codecs.{DocumentCodecProvider, IterableCodecProvider}
import org.reactivestreams.Publisher

import scala.collection.immutable
import scala.concurrent.Future
import scala.reflect.ClassTag

class MongoTemplate private (val client: MongoClient) extends AutoCloseable {
  def startSession(): Publisher[ClientSession] = client.startSession()

  def startSession(options: ClientSessionOptions): Publisher[ClientSession] = client.startSession(options)

  def getDatabase(name: String): MongoDatabase = client.getDatabase(name)

  def getCollection(dbName: String, collName: String): MongoCollection[Document] =
    getDatabase(dbName).getCollection(collName)

  def getCollection[T](
      dbName: String,
      collName: String,
      codecProviders: Seq[CodecProvider],
      codecRegistry: CodecRegistry = MongoTemplate.DEFAULT_CODEC_REGISTRY)(
      implicit ev1: ClassTag[T]): MongoCollection[T] = {
    val list = new java.util.LinkedList[CodecRegistry]()
    list.add(fromProviders(codecProviders: _*))
    list.add(codecRegistry)
    val cc = fromRegistries(list)
    getDatabase(dbName).getCollection(collName, ev1.runtimeClass).withCodecRegistry(cc).asInstanceOf[MongoCollection[T]]
  }

  def listDatabaseNames(): Publisher[String] = client.listDatabaseNames()

  def listDatabaseNamesAsync()(implicit mat: Materializer): Future[immutable.Seq[String]] =
    Source.fromPublisher(client.listDatabaseNames()).runWith(Sink.seq)

  def listDatabaseNames(clientSession: ClientSession): Publisher[String] = client.listDatabaseNames(clientSession)

  def listDatabaseNamesAsync(clientSession: ClientSession)(implicit mat: Materializer): Future[immutable.Seq[String]] =
    Source.fromPublisher(client.listDatabaseNames(clientSession)).runWith(Sink.seq)

  override def close(): Unit = client.close()
}

object MongoTemplate {

  val DEFAULT_CODEC_REGISTRY: CodecRegistry = fromRegistries(
    MongoClients.getDefaultCodecRegistry,
    fromProviders(DocumentCodecProvider(), IterableCodecProvider()),
    fromProviders(new FusionCodecProvider()))

  def apply(client: MongoClient): MongoTemplate = new MongoTemplate(client)
}
