package fusion.data.mongodb

import com.mongodb.async.client.MongoClients
import fusion.data.mongodb.codec.FusionCodecProvider
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.{DocumentCodecProvider, IterableCodecProvider}

import scala.reflect.ClassTag

class MongoTemplate private (val client: MongoClient) extends AutoCloseable {
  def startSession(): SingleObservable[ClientSession] = client.startSession()

  def startSession(options: ClientSessionOptions): SingleObservable[ClientSession] = client.startSession(options)

  def getDatabase(name: String): MongoDatabase = client.getDatabase(name)

  def getCollection(dbName: String, collName: String): MongoCollection[Document] =
    getDatabase(dbName).getCollection(collName)

  def getCollection[T: ClassTag](
      dbName: String,
      collName: String,
      codecProviders: Seq[CodecProvider],
      codecRegistry: CodecRegistry = MongoTemplate.DEFAULT_CODEC_REGISTRY): MongoCollection[T] = {
    val list = new java.util.LinkedList[CodecRegistry]()
    list.add(fromProviders(codecProviders: _*))
    list.add(codecRegistry)
    val cc = fromRegistries(list)
    getDatabase(dbName).getCollection[T](collName).withCodecRegistry(cc)
  }

  def listDatabaseNames(): Observable[String] = client.listDatabaseNames()

  def listDatabaseNames(clientSession: ClientSession): Observable[String] = client.listDatabaseNames(clientSession)

  override def close(): Unit = client.close()
}

object MongoTemplate {

  val DEFAULT_CODEC_REGISTRY: CodecRegistry = fromRegistries(
    MongoClients.getDefaultCodecRegistry,
    fromProviders(new FusionCodecProvider()),
    fromProviders(DocumentCodecProvider(), IterableCodecProvider())
  )

  def apply(client: MongoClient): MongoTemplate = new MongoTemplate(client)
}
