package docs.study

trait Sub {
  def onMessage(topic: String, message: Object): Unit
  def onUnsubscribe(topic: String): Unit
}

object PubSubs {
  private var _topics                       = Map[String, Set[Sub]]()
  private def topics: Map[String, Set[Sub]] = synchronized(_topics)
  private def topics_=(v: Map[String, Set[Sub]]): Unit = synchronized {
    _topics = v
  }

  def subscribe(topic: String, sub: Sub): Unit =
    topics = topics.updated(topic, topics.getOrElse(topic, Set()) + sub)

  def unsubscribe(topic: String, sub: Sub): Unit =
    topics.get(topic).foreach { subs =>
      topics = topics.updated(topic, subs.filterNot(item => item == sub))
    }

  def clearByTopic(topic: String): Unit =
    topics.get(topic).foreach { subs =>
      topics = topics.updated(topic, Set())
      subs.foreach(sub => sub.onUnsubscribe(topic))
    }

  /**
   * @return 返回send subscribe size
   */
  def send(topic: String, message: Object): Int =
    topics.get(topic) match {
      case Some(subs) =>
        subs.foreach(sub => sub.onMessage(topic, message))
        subs.size
      case None => // topic not exists
        0
    }

}

object PubSubDemo extends App {
  case class Subscribe(name: String) extends Sub {
    override def onMessage(topic: String, message: Object): Unit = println(s"$name 收到 $topic 消息：$message")
    override def onUnsubscribe(topic: String): Unit              = println(s"$name 被 tickOff 主题：$topic")
  }

  val sub1   = Subscribe("张三")
  val sub2   = Subscribe("李四")
  val topic1 = "topic1"
  val topic2 = "topic2"
  PubSubs.subscribe(topic1, sub1)
  PubSubs.subscribe(topic1, sub2)
  PubSubs.send(topic1, "中少红卡")
  PubSubs.send(topic2, "红广少年")

  println("-------------------")

  PubSubs.subscribe(topic2, sub2)
  PubSubs.send(topic2, "红广少年")
  println("-------------------")

  PubSubs.unsubscribe(topic1, sub1)
  PubSubs.send(topic1, "中少红卡")
}
