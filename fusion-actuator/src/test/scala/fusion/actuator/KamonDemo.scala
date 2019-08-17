package fusion.actuator

import kamon.Kamon

object KamonDemo {

  def main(args: Array[String]): Unit = {
    Kamon.init()
  }
}
