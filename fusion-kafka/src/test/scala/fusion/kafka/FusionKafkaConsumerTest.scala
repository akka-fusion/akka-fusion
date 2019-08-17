package fusion.kafka

import java.time.LocalTime
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Await
import scala.concurrent.duration._

class FusionKafkaConsumerTest extends TestKit(ActorSystem()) with FusionTestFunSuite {
  import system.dispatcher
  implicit private val mat = ActorMaterializer()

  test("FusionKafkaConsumer") {
    val control = Consumer
      .plainSource(FusionKafkaConsumer(system).consumer, Subscriptions.topics("test"))
      .groupedWithin(100, 30.seconds)
      .map { records =>
        println(s"${LocalTime.now()} size ${records.size}")
        records.size
      }
      .toMat(Sink.fold(0L)((n, size) => n + size))(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()

    TimeUnit.SECONDS.sleep(10)
    val producer = FusionKafkaProducer(system).producer.createKafkaProducer()
    (1 to 20).foreach { c =>
      producer.send(new ProducerRecord[String, String]("test", c.toString))
      TimeUnit.MILLISECONDS.sleep(50)
    }
    println(LocalTime.now() + " produce complete ")
    TimeUnit.SECONDS.sleep(5)
    println(LocalTime.now() + " begin drain shutdown")
    val f      = control.drainAndShutdown()
    val result = Await.result(f, Duration.Inf)
    println(s"${LocalTime.now()} complete drain shutdown: $result")
    producer.close()
  }

}
