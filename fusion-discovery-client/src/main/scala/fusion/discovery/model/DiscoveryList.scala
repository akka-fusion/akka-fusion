package fusion.discovery.model

import helloscala.common.jackson.Jackson

case class DiscoveryList[T](data: Seq[T], count: Int) {
  override def toString: String = Jackson.stringify(this)
}
