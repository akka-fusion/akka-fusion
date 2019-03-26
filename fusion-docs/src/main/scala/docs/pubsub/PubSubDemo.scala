package docs.pubsub

import java.util.concurrent.atomic.AtomicLong

class PubSubs private (val idGenerator: AtomicLong) {
  private var topics      = Map[String, Set[Long]]()
  private var subscribies = Map[Long, PubSubs.PubSubFunc]()
}

object PubSubs {
  type PubSubFunc = PubSubMessage => Unit
  private val ins = new PubSubs(new AtomicLong(0L))

  case class PubSubMessage(id: Long, topic: String, message: Any)
  final class Cancellable(val id: Long, instance: PubSubs) {
    def unsubscribe(): Unit = instance.subscribies -= id
  }

  def addTopic(topic: String): Unit =
    if (!ins.topics.contains(topic)) {
      ins.topics += (topic -> Set())
    }

  def removeTopic(topic: String): Unit =
    ins.topics.get(topic).foreach { subIds =>
      ins.topics -= topic
      ins.subscribies --= subIds
    }

  def subscribe(topic: String, func: PubSubFunc): Cancellable = {
    addTopic(topic)
    val id = ins.idGenerator.incrementAndGet()
    ins.topics += Tuple2(topic, ins.topics(topic) + id)
    ins.subscribies += (id -> func)
    new Cancellable(id, ins)
  }

  def send(topic: String, message: Any): Unit =
    for {
      subIds <- ins.topics.get(topic)
      subId  <- subIds
      func   <- ins.subscribies.get(subId)
    } func(PubSubMessage(subId, topic, message))

  def send(message: Any): Unit = ins.subscribies.foreach {
    case (id, func) => func(PubSubMessage(id, "", message))
  }

}

object PubSubDemo extends App {
  PubSubs.addTopic("test1")
  PubSubs.addTopic("test2")

  val cancel1  = PubSubs.subscribe("test1", msg => println(s"1 : $msg"))
  val cancel1b = PubSubs.subscribe("test1", msg => println(s"1b: $msg"))
  val cancel2  = PubSubs.subscribe("test2", msg => println(s"2 : $msg"))
  val cancel3  = PubSubs.subscribe("test3", msg => println(s"3 : $msg"))

  PubSubs.send("test1", "test1 message")
  PubSubs.send("test2", "test2 message")
  PubSubs.send("test3", "test3 message")
  PubSubs.send("broadcast message")

  println("-------------cancel 1")
  cancel1.unsubscribe()
  PubSubs.send("test1", "test1 message")

  println("-------------cancel 2")
  cancel1b.unsubscribe()
  PubSubs.send("test1", "test1 message")

  cancel2.unsubscribe()
  cancel3.unsubscribe()
  PubSubs.send("not subscriber received message")
}
