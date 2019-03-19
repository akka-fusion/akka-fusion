package fusion.discovery.model

sealed trait DiscoveryEvent

case class DiscoveryNamingEvent(serviceName: String, instances: Seq[DiscoveryInstance]) extends DiscoveryEvent {}
