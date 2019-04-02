package fusion.inject

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Module
import com.typesafe.config.ConfigFactory
import helloscala.common.util.StringUtils
import javax.inject.Named

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.reflect.ClassTag

object Injects {
  lazy val injector: Injector = Guice.createInjector(generateModules(): _*)

  private def generateModules(): mutable.Seq[Module] =
    ConfigFactory
      .load()
      .getStringList("fusion.module")
      .asScala
      .distinct
      .filter(s => StringUtils.isNoneBlank(s))
      .map(t => Class.forName(t).newInstance().asInstanceOf[Module])

  /**
   * 根据类型获类实例
   * @param ev 类型
   * @tparam T ClassTag[T]
   * @return
   */
  def instance[T](implicit ev: ClassTag[T]): T =
    injector.getInstance(ev.runtimeClass).asInstanceOf[T]

  /**
   * 根据类型及注解获取类实例
   * @param annotation 注解
   * @param ev 类实例
   * @tparam T ClassTag[T]
   * @return
   */
  def instance[T](annotation: Named)(implicit ev: ClassTag[T]): T =
    injector.getInstance(Key.get(ev.runtimeClass, annotation)).asInstanceOf[T]

  /**
   * Java Api
   * @param c 类实例
   * @return
   */
  def getInstance[T](c: Class[T]): T = injector.getInstance(c).asInstanceOf[T]

  /**
   * Java Api
   * @param key Guice Key
   * @return
   */
  def getInstance[T](key: Key[T]): T = injector.getInstance(key).asInstanceOf[T]

  /**
   * Java Api
   * @param c 类实例
   * @param a 命名注解
   * @return
   */
  def getInstance[T](c: Class[T], a: Named): T = injector.getInstance(Key.get(c, a)).asInstanceOf[T]
}

trait InjectSupport {

  def instance[T](implicit ev: ClassTag[T]): T = Injects.instance[T]

  def instance[T](a: Named)(implicit ev: ClassTag[T]): T =
    Injects.instance[T](a)

  def getInstance[T](c: Class[T]): T = Injects.getInstance(c)

  def getInstance[T](key: Key[T]): T = Injects.getInstance(key)

  def getInstance[T](c: Class[T], a: Named): T = Injects.getInstance(Key.get(c, a))

}
