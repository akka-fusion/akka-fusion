/*
 * Copyright 2019 helloscala.com
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

package fusion.inject.guice

import com.google.inject._
import helloscala.common.Configuration
import helloscala.common.util.StringUtils
import javax.inject.Named

import scala.collection.immutable
import scala.reflect.ClassTag

private[fusion] class FusionInjector(configuration: Configuration, module: AbstractModule) {
  val injector: Injector = Guice.createInjector(Stage.PRODUCTION, generateModules(configuration): _*)

  private def generateModules(configuration: Configuration): immutable.Seq[Module] = {
    val disabled = configuration.get[Seq[String]]("fusion.inject.modules.disabled").toSet
    val cl = Thread.currentThread().getContextClassLoader
    module +: configuration
      .get[Seq[String]]("fusion.inject.modules.enabled")
      .filter(s => StringUtils.isNoneBlank(s) && !disabled(s))
      .distinct
      .map(t => cl.loadClass(t).getConstructor().newInstance().asInstanceOf[Module])
      .toVector
  }

  /**
   * 根据类型获取类实例
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

  def instance[T](annotation: com.google.inject.name.Named)(implicit ev: ClassTag[T]): T =
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

  def getInstance[T](c: Class[T], a: com.google.inject.name.Named): T =
    injector.getInstance(Key.get(c, a)).asInstanceOf[T]
}
