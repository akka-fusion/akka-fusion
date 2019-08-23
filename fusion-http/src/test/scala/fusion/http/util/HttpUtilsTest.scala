package fusion.http.util

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Authority
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import fusion.http.HttpSourceQueue
import fusion.test.FusionTestFunSuite

class HttpUtilsTest extends TestKit(ActorSystem()) with FusionTestFunSuite {
  test("authority") {
    val a = Authority.parse("hongka-server-account")
    println(a)
    println(a.host)
    println(a.host.address())
  }

  test("testForExtension") {
    HttpUtils.customMediaTypes must not be empty
    HttpUtils.customMediaTypes.map(_._2.binary) must contain(true)
  }

  test("copyUri") {
    implicit val mat       = ActorMaterializer()
    val httpSourceQueueMap = new ConcurrentHashMap[Authority, HttpSourceQueue]()
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.9:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.9", 8888, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.8:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.8", 8888, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.7:8097"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.7", 8097, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.9:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.9", 8888, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.8:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.8", 8888, 512)
      println(s"new queue: $q")
      q
    })

    httpSourceQueueMap.forEach((a, q) => println(s"$a  <->  $q"))
  }

}
