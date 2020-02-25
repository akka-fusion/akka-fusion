/*
 * Copyright 2019 akka-fusion.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.inject

import com.google.inject.{ Guice, Injector, Key, Module, Stage }
import com.typesafe.config.ConfigFactory
import helloscala.common.util.StringUtils
import javax.inject.Named

import scala.collection.immutable
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

object FusionInjector {
  lazy val injector: Injector = Guice.createInjector(Stage.PRODUCTION, generateModules(): _*)

  private def generateModules(): immutable.Seq[Module] =
    ConfigFactory
      .load()
      .getStringList("fusion.inject.modules")
      .asScala
      .view
      .distinct
      .filter(s => StringUtils.isNoneBlank(s))
      .map(t => Class.forName(t).getConstructor().newInstance().asInstanceOf[Module])
      .toVector

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
  def instance[T](implicit ev: ClassTag[T]): T = FusionInjector.instance[T]

  def instance[T](a: Named)(implicit ev: ClassTag[T]): T =
    FusionInjector.instance[T](a)

  def getInstance[T](c: Class[T]): T = FusionInjector.getInstance(c)

  def getInstance[T](key: Key[T]): T = FusionInjector.getInstance(key)

  def getInstance[T](c: Class[T], a: Named): T = FusionInjector.getInstance(Key.get(c, a))
}
