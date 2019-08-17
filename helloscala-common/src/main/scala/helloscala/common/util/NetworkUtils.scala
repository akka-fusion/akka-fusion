package helloscala.common.util

import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.InterfaceAddress
import java.net.NetworkInterface

import scala.collection.JavaConverters._

object NetworkUtils {
  private val validNetworkNamePrefixes = List("eth", "enp", "wlp")
  def validNetworkName(name: String)   = validNetworkNamePrefixes.exists(prefix => name.startsWith(prefix))

  def interfaces(): Vector[NetworkInterface] = NetworkInterface.getNetworkInterfaces.asScala.toVector

  def onlineNetworkInterfaces() = {
    interfaces().filterNot(ni =>
      ni.isLoopback || !ni.isUp || ni.isVirtual || ni.isPointToPoint || !validNetworkName(ni.getName))
  }

  def onlineInterfaceAddress(): Vector[InterfaceAddress] = {
    onlineNetworkInterfaces().flatMap(i => i.getInterfaceAddresses.asScala)
  }

  def firstOnlineInet4Address(): Option[InetAddress] = {
    onlineInterfaceAddress().view.filter(ia => ia.getAddress.isInstanceOf[Inet4Address]).map(_.getAddress).headOption
  }

  def toInetSocketAddress(address: String, defaultPort: Int): InetSocketAddress = address.split(':') match {
    case Array(host, AsInt(port)) => InetSocketAddress.createUnresolved(host, port)
    case Array(host)              => InetSocketAddress.createUnresolved(host, defaultPort)
    case _                        => throw new ExceptionInInitializerError(s"无效的通信地址：$address")
  }

}
