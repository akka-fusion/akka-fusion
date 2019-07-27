package helloscala.common.util

import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.InterfaceAddress
import java.net.NetworkInterface

import scala.collection.JavaConverters._

object NetworkUtils {

  def interfaces(): Vector[NetworkInterface] = NetworkInterface.getNetworkInterfaces.asScala.toVector

  def onlineInterfaceAddress(): Vector[InterfaceAddress] = {
    interfaces().view
      .filterNot(_.isLoopback)
      .filterNot(_.isPointToPoint)
      .filter(_.isUp)
      .flatMap(i => i.getInterfaceAddresses.asScala)
      .toVector
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
