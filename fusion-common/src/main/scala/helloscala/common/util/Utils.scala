package helloscala.common.util

import java.lang.management.ManagementFactory
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.{LocalDate, LocalDateTime}
import java.util.Properties
import java.util.concurrent.ThreadLocalRandom

import scala.util.Try
import scala.util.control.NonFatal
import scala.util.matching.Regex

object Utils {

  val REGEX_DIGIT: Regex = """[\d,]+""".r
  val RANDOM_CHARS: IndexedSeq[Char] = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')

  val random: SecureRandom = new SecureRandom()

  def using[T <: AutoCloseable, R](res: T)(func: T => R): R = {
    assert(res != null, "Resource res must not null")
    try {
      func(res)
    } finally {
      res.close()
    }
  }

  def swap[X, Y](x: X, y: Y): (Y, X) = (y, x)

  /**
   * 获取当前进程 pid
   */
  @inline def getPid: Long =
    java.lang.Long.parseLong(ManagementFactory.getRuntimeMXBean.getName.split("@")(0))

  def either[T <: Throwable, R](func: => R): Either[T, R] =
    try {
      val result = func
      Right(result)
    } catch {
      case NonFatal(e) =>
        Left(e.asInstanceOf[T])
    }

  /**
   * 将字符串解析为数字
   *
   * @param s 字符串
   * @return
   */
  def parseInt(s: CharSequence): Option[Int] =
    REGEX_DIGIT.findFirstIn(s).map(_.replaceAll(",", "").toInt)

  def parseInt(s: CharSequence, deft: => Int): Int =
    parseInt(s).getOrElse(deft)

  def parseInt(a: Any, deft: => Int): Int =
    parseInt(a.toString, deft)

  def parseIntAll(s: CharSequence): List[Int] = {
    val iter = REGEX_DIGIT.findAllIn(s)
    var list = List.empty[Int]
    while (iter.hasNext) {
      list = iter.next().toInt :: list
    }
    list
  }

  def parseLong(s: Any, deft: => Long): Long = parseLong(s).getOrElse(deft)

  def parseLong(s: Any): Option[Long] = s match {
    case l: Long    => Some(l)
    case i: Int     => Some(i.toLong)
    case s: String  => Try(s.toLong).toOption
    case bi: BigInt => Some(bi.longValue())
    case _          => None
  }

  def isNoneBlank(content: String): Boolean = !isBlank(content)

  def isBlank(content: String): Boolean =
    content == null || content.isEmpty || content.forall(Character.isWhitespace)

  def byteBufferToArray(buf: ByteBuffer): Array[Byte] = {
    val dst = new Array[Byte](buf.remaining())
    buf.get(dst)
    dst
  }

  def randomString(n: Int): String = {
    val len = RANDOM_CHARS.length
    val sb = new StringBuilder
    var i = 0
    while (i < n) {
      i += 1
      val idx = ThreadLocalRandom.current().nextInt(len)
      val c = RANDOM_CHARS.apply(idx)
      sb.append(c)
    }
    sb.toString()
  }

  def randomBytes(size: Int): Array[Byte] = {
    val buf = new Array[Byte](size)
    random.nextBytes(buf)
    buf
  }

  def parseSeq(str: String, splitChar: Char = ','): Vector[String] =
    str.split(splitChar).filter(s => StringUtils.isNoneBlank(s)).toVector

  def mapToJMap[K, V](map: Map[K, V]): java.util.Map[K, V] = {
    val m = new java.util.HashMap[K, V]()
    map.foreach { case (k, v) => m.put(k, v) }
    m
  }

  def boxed(v: Any): Object = v match {
    case i: Int      => Int.box(i)
    case l: Long     => Long.box(l)
    case d: Double   => Double.box(d)
    case s: Short    => Short.box(s)
    case f: Float    => Float.box(f)
    case c: Char     => Float.box(c)
    case b: Boolean  => Boolean.box(b)
    case b: Byte     => Byte.box(b)
    case obj: AnyRef => obj
    case o           => o.asInstanceOf[Object]
  }

  def sqlBoxed(v: Any): Object = v match {
    case ldt: LocalDateTime => TimeUtils.toSqlTimestamp(ldt)
    case ld: LocalDate      => TimeUtils.toSqlDate(ld)
    case o                  => o.asInstanceOf[Object]
  }

  def boxedSQL(v: Any): Object =
    try {
      boxed(v)
    } catch {
      case _: Throwable =>
        sqlBoxed(v)
    }

  def isEmail(account: String): Boolean =
    // TODO
    account.contains('@')

  @inline
  def option(s: String): Option[String] =
    Some(s).filter(str => StringUtils.isNoneBlank(str))

  @inline
  def option[V](v: V): Option[V] = Option(v)

  def propertiesToMap(props: Properties): Map[String, String] = {
    import scala.collection.JavaConverters._
    props
      .stringPropertyNames()
      .asScala
      .map(name => name -> props.getProperty(name))
      .toMap
  }

  def closeQuiet(io: AutoCloseable): Unit = {
    if (io ne null) try {
      io.close()
    } catch {
      case _: Throwable => // do nothing
    }
  }

  def some[T](v: T): Option[T] = Option(v)

  def test(): Unit = {
    val 1 = 1
    val 1 = 2
    val 3 = 3
    val 8239 = 43

    val s: Option[String] = null
    s match {
      case Some(v) => println("Some(v)")
      case _       => println("None")
    }
  }

}
