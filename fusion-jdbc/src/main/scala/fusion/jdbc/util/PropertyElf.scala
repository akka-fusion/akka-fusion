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

package fusion.jdbc.util

import java.lang.reflect.Method
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.Locale
import java.util.Properties

import com.typesafe.scalalogging.StrictLogging
import com.zaxxer.hikari.HikariConfig
import helloscala.common.util.Utils

object PropertyElf extends StrictLogging {
  private val GETTER_PATTERN: Pattern = Pattern.compile("(get|is)[A-Z].+")

  def setTargetFromProperties(target: Any, properties: Properties): Unit = {
    if (target == null || properties == null) return
    val methods: java.util.List[Method] = java.util.Arrays.asList(target.getClass.getMethods(): _*)
    properties.forEach((key: Any, value: Any) => {
      if (target.isInstanceOf[HikariConfig] && key.toString.startsWith("dataSource."))
        target.asInstanceOf[HikariConfig].addDataSourceProperty(key.toString.substring("dataSource.".length), value)
      else setProperty(target, key.toString, Utils.boxed(value), methods)
    })
  }

  /**
   * Get the bean-style property names for the specified object.
   *
   * @param targetClass the target object
   * @return a set of property names
   */
  def getPropertyNames(targetClass: Class[_]): java.util.Set[String] = {
    val set = new java.util.HashSet[String]
    val matcher: Matcher = GETTER_PATTERN.matcher("")
    for (method <- targetClass.getMethods) {
      var name: String = method.getName
      if (method.getParameterTypes.length == 0 && matcher.reset(name).matches) {
        name = name.replaceFirst("(get|is)", "")
        try if (targetClass.getMethod("set" + name, method.getReturnType) != null) {
          name = Character.toLowerCase(name.charAt(0)) + name.substring(1)
          set.add(name)
        } catch {
          case e: Exception =>
          // fall thru (continue)
        }
      }
    }
    set
  }

  def getProperty(propName: String, target: Any): Any =
    try { // use the english locale to avoid the infamous turkish locale bug
      val capitalized: String = "get" + propName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propName.substring(1)
      val method: Method = target.getClass.getMethod(capitalized)
      method.invoke(target)
    } catch {
      case e: Exception =>
        try {
          val capitalized: String = "is" + propName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propName.substring(1)
          val method: Method = target.getClass.getMethod(capitalized)
          method.invoke(target)
        } catch {
          case e2: Exception =>
            null
        }
    }

  def copyProperties(props: Properties): Properties = {
    val copy: Properties = new Properties
    props.forEach((key: Any, value: Any) => copy.setProperty(key.toString, value.toString))
    copy
  }

  private def setProperty(target: Any, propName: String, propValue: Object, methods: java.util.List[Method]): Unit = {
    val methodName: String = "set" + propName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propName.substring(1)
    var writeMethod: Method =
      methods.stream.filter((m: Method) => m.getName == methodName && m.getParameterCount == 1).findFirst.orElse(null)
    if (writeMethod == null) {
      val methodName2: String = "set" + propName.toUpperCase(Locale.ENGLISH)
      writeMethod = methods.stream
        .filter((m: Method) => m.getName == methodName2 && m.getParameterCount == 1)
        .findFirst
        .orElse(null)
    }
    if (writeMethod == null) {
      logger.error("Property {} does not exist on target {}", propName, target.getClass)
      throw new RuntimeException(String.format("Property %s does not exist on target %s", propName, target.getClass))
    }
    try {
      val paramClass: Class[_] = writeMethod.getParameterTypes().apply(0)
      if (paramClass eq classOf[Int]) writeMethod.invoke(target, Int.box(propValue.toString.toInt))
      else if (paramClass eq classOf[Long]) writeMethod.invoke(target, java.lang.Long.valueOf(propValue.toString))
      else if ((paramClass eq classOf[Boolean]) || (paramClass eq classOf[Boolean]))
        writeMethod.invoke(target, java.lang.Boolean.valueOf(propValue.toString))
      else if (paramClass eq classOf[String]) writeMethod.invoke(target, propValue.toString)
      else
        try {
          logger.debug("Try to create a new instance of \"{}\"", propValue.toString)
          val arg = Class.forName(propValue.toString).newInstance().asInstanceOf[Object]
          writeMethod.invoke(target, arg)
        } catch {
          case e @ (_: InstantiationException | _: ClassNotFoundException) =>
            logger.debug("Class \"{}\" not found or could not instantiate it (Default constructor)", propValue.toString)
            writeMethod.invoke(target, propValue)
        }
    } catch {
      case e: Exception =>
        logger.error("Failed to set property {} on target {}", propName, target.getClass, e)
        throw new RuntimeException(e)
    }
  }
}
