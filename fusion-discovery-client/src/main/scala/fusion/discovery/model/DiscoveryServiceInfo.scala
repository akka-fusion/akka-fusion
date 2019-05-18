package fusion.discovery.model

case class DiscoveryServiceInfo(
    name: String,
    groupName: String,
    clusters: String,
    cacheMillis: Long = 1000L,
    hosts: Seq[DiscoveryInstance] = Nil,
    lastRefTime: Long = 0L,
    checksum: String = "",
    allIPs: Boolean = false) {
  def ipCount: Int     = hosts.size
  def expired: Boolean = System.currentTimeMillis - lastRefTime > cacheMillis
  def isValid: Boolean = hosts != null
}
