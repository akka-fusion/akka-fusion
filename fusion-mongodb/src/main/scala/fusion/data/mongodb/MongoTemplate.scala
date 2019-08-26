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

package fusion.data.mongodb

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.mongodb.ClientSessionOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.reactivestreams.client._
import fusion.data.mongodb.codec.FusionCodecProvider
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.DocumentCodecProvider
import org.mongodb.scala.bson.codecs.IterableCodecProvider
import org.reactivestreams.Publisher

import scala.collection.immutable
import scala.concurrent.Future
import scala.reflect.ClassTag

class MongoTemplate private (val client: MongoClient, defaultDbName: String) extends AutoCloseable {
  def startSession(): Publisher[ClientSession] = client.startSession()

  def startSession(options: ClientSessionOptions): Publisher[ClientSession] = client.startSession(options)

  def getDatabase(name: String): MongoDatabase = client.getDatabase(name)
  def defaultDatabase: MongoDatabase = client.getDatabase(defaultDbName)

  def getCollection(collName: String): MongoCollection[Document] = getDatabaseCollection(defaultDbName, collName)

  def getDatabaseCollection(dbName: String, collName: String): MongoCollection[Document] =
    getDatabase(dbName).getCollection(collName)

  def getCollection[T](
      collName: String,
      codecProviders: Seq[CodecProvider],
      codecRegistry: CodecRegistry = MongoTemplate.DEFAULT_CODEC_REGISTRY)(
      implicit ev1: ClassTag[T]): MongoCollection[T] =
    getDatabaseCollection(defaultDbName, collName, codecProviders, codecRegistry)

  def getDatabaseCollection[T](
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

  def apply(client: MongoClient): MongoTemplate = apply(client, "test")

  def apply(client: MongoClient, defaultDatabase: String): MongoTemplate =
    new MongoTemplate(client, if (defaultDatabase eq null) "test" else defaultDatabase)

  def replaceOptions: ReplaceOptions = new ReplaceOptions().upsert(true)
}
