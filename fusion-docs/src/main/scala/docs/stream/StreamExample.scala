package docs.stream

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import helloscala.common.jackson.Jackson
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future
import scala.util.Random

object StreamExample {
  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  import system.dispatcher

  def findOrgIdsByArea(area: String): Future[List[String]] = Future {
    (0 until Random.nextInt(50)).map(_.toString).toList
  }

  def findUserIdsByOrgId(orgId: String): Future[List[String]] = Future {
    (0 until Random.nextInt(50)).map(n => s"$orgId-$n").toList
  }

  def findImeisByUserIds(userIds: Iterable[String]): Future[List[String]] = Future {
    userIds.map(id => "imei-" + id).toList
  }

  def batchSendMessage(imeis: Seq[String], content: String): Unit = {
    println(s"发送消息内容为：$content, imeis个数：${imeis.size}")
  }

  def firstOnFuture(): Future[Unit] = {
    findOrgIdsByArea("北京")
      .flatMap { orgIds =>
        val futures = orgIds.map(id => findUserIdsByOrgId(id))
        Future.sequence(futures)
      }
      .flatMap { orgUserIdList =>
        val futures = orgUserIdList.map(userIds => findImeisByUserIds(userIds))
        Future.sequence(futures)
      }
      .map { orgImeiList =>
        orgImeiList.foreach(imeis => batchSendMessage(imeis, "推送消息"))
      }
  }

  def secondOnAkkaStream(): Future[Done] = {
    Source
      .fromFuture(findOrgIdsByArea("北京"))
      .mapConcat(identity)
      .mapAsync(4)(orgId => findUserIdsByOrgId(orgId))
      .mapAsync(4)(userIds => findImeisByUserIds(userIds))
      .mapConcat(identity)
      .grouped(1000)
      .runForeach(imeis => batchSendMessage(imeis, "推送消息"))
  }

  def secondOnAkkaStreamThrottle(): Future[Done] = {
    import scala.concurrent.duration._
    Source
      .fromFuture(findOrgIdsByArea("北京"))
      .mapConcat(identity)
      .mapAsync(4)(orgId => findUserIdsByOrgId(orgId))
      .mapAsync(4)(userIds => findImeisByUserIds(userIds))
      .mapConcat(identity)
      .grouped(1000)
      .throttle(5, 10.seconds)
      .runForeach(imeis => batchSendMessage(imeis, "推送消息"))
  }

  case class SendMessageByArea(area: String, content: String)

  def secondOnAkkaStreamKafka(): Future[Done] = {
    import scala.concurrent.duration._
    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics("message"))
      .map(record => Jackson.convertValue[SendMessageByArea](record.value()))
      .flatMapConcat { req =>
        Source
          .fromFuture(findOrgIdsByArea(req.area))
          .mapConcat(identity)
          .mapAsync(4)(orgId => findUserIdsByOrgId(orgId))
          .mapAsync(4)(userIds => findImeisByUserIds(userIds))
          .mapConcat(identity)
          .grouped(1000)
          .throttle(5, 10.seconds)
          .map(imeis => batchSendMessage(imeis, req.content))
      }
      .runWith(Sink.ignore)
  }

}
