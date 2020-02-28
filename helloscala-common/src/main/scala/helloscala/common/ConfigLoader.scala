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

package helloscala.common

import java.nio.file.{ Path, Paths }
import java.time.OffsetDateTime
import java.util.Properties

import com.typesafe.config._
import helloscala.common.util.{ StringUtils, TimeUtils }

import scala.collection.mutable
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/**
 * A Config loader
 */
trait ConfigLoader[A] { self =>
  def load(config: Config, path: String = ""): A

  def map[B](f: A => B): ConfigLoader[B] = (config: Config, path: String) => f(self.load(config, path))
}

object ConfigLoader {
  def apply[A](f: Config => String => A): ConfigLoader[A] = (config: Config, path: String) => f(config)(path)

  implicit val stringLoader: ConfigLoader[String] = ConfigLoader(_.getString)

  implicit val seqStringLoader: ConfigLoader[Seq[String]] =
    ConfigLoader(_.getStringList).map(_.asScala.toVector)

  implicit val arrayStringLoader: ConfigLoader[Array[String]] =
    ConfigLoader(_.getStringList).map { list =>
      val arr = new Array[String](list.size())
      list.toArray(arr)
      arr
    }

  implicit val intLoader: ConfigLoader[Int] = ConfigLoader(c => {
    path: String => {
      try {
        c.getInt(path)
      } catch {
        case NonFatal(_) =>
          c.getString(path).toInt
      }
    }
  })

  implicit val seqIntLoader: ConfigLoader[Seq[Int]] =
    ConfigLoader(_.getIntList).map(_.asScala.map(_.toInt).toVector)

  implicit val booleanLoader: ConfigLoader[Boolean] = ConfigLoader(_.getBoolean)

  implicit val seqBooleanLoader: ConfigLoader[Seq[Boolean]] =
    ConfigLoader(_.getBooleanList).map(_.asScala.map(_.booleanValue).toVector)

  implicit val durationLoader: ConfigLoader[Duration] = ConfigLoader { config => path =>
    if (!config.getIsNull(path)) config.getDuration(path).toNanos.nanos
    else Duration.Inf
  }

  // Note: this does not support null values but it added for convenience
  implicit val seqDurationLoader: ConfigLoader[Seq[Duration]] =
    ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos).toVector)

  implicit val finiteDurationLoader: ConfigLoader[FiniteDuration] =
    ConfigLoader(_.getDuration).map(_.toNanos.nanos)

  implicit val seqFiniteDurationLoader: ConfigLoader[Seq[FiniteDuration]] =
    ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos).toVector)

  implicit val doubleLoader: ConfigLoader[Double] = ConfigLoader(_.getDouble)

  implicit val seqDoubleLoader: ConfigLoader[Seq[Double]] =
    ConfigLoader(_.getDoubleList).map(_.asScala.map(_.doubleValue).toVector)

  implicit val numberLoader: ConfigLoader[Number] = ConfigLoader(_.getNumber)

  implicit val seqNumberLoader: ConfigLoader[Seq[Number]] =
    ConfigLoader(_.getNumberList).map(_.asScala.toVector)

  implicit val longLoader: ConfigLoader[Long] = ConfigLoader(_.getLong)

  implicit val seqLongLoader: ConfigLoader[Seq[Long]] =
    ConfigLoader(_.getDoubleList).map(_.asScala.map(_.longValue).toVector)

  implicit val bytesLoader: ConfigLoader[ConfigMemorySize] = ConfigLoader(_.getMemorySize)

  implicit val seqBytesLoader: ConfigLoader[Seq[ConfigMemorySize]] =
    ConfigLoader(_.getMemorySizeList).map(_.asScala.toVector)

  implicit val configLoader: ConfigLoader[Config] = ConfigLoader(_.getConfig)
  implicit val configListLoader: ConfigLoader[ConfigList] = ConfigLoader(_.getList)
  implicit val configObjectLoader: ConfigLoader[ConfigObject] = ConfigLoader(_.getObject)

  implicit val seqConfigLoader: ConfigLoader[Seq[Config]] =
    ConfigLoader(_.getConfigList).map(_.asScala.toVector)

  implicit val configurationLoader: ConfigLoader[Configuration] =
    configLoader.map(c => new Configuration(c))

  implicit val seqConfigurationLoader: ConfigLoader[Seq[Configuration]] =
    seqConfigLoader.map(_.map(c => new Configuration(c)))

  /**
   * Loads a value, interpreting a null value as None and any other value as Some(value).
   */
  implicit def optionLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Option[A]] =
    (config: Config, path: String) => {
      if (!config.hasPath(path) || config.getIsNull(path)) None
      else {
        val value = valueLoader.load(config, path)
        Some(value)
      }
    }

  implicit val propertiesLoader: ConfigLoader[Properties] =
    new ConfigLoader[Properties] {
      def make(props: Properties, parentKeys: String, obj: ConfigObject): Unit =
        obj.keySet().forEach { key =>
          val value = obj.get(key)
          val propKey =
            if (StringUtils.isNoneBlank(parentKeys)) parentKeys + "." + key
            else key
          value.valueType() match {
            case ConfigValueType.OBJECT =>
              make(props, propKey, value.asInstanceOf[ConfigObject])
            case _ =>
              props.put(propKey, value.unwrapped())
          }
        }

      override def load(config: Config, path: String): Properties = {
        val obj =
          if (StringUtils.isBlank(path)) config.root()
          else config.getObject(path)
        val props = new Properties()
        make(props, "", obj)
        props
      }
    }

  implicit val scalaMapLoader: ConfigLoader[Map[String, String]] =
    new ConfigLoader[Map[String, String]] {
      def make(props: mutable.Map[String, String], parentKeys: String, obj: ConfigObject): Unit =
        obj.keySet().forEach { key: String =>
          val value = obj.get(key)
          val propKey =
            if (StringUtils.isNoneBlank(parentKeys)) parentKeys + "." + key
            else key
          value.valueType() match {
            case ConfigValueType.OBJECT =>
              make(props, propKey, value.asInstanceOf[ConfigObject])
            case _ =>
              props.put(propKey, value.unwrapped().toString)
          }
        }

      override def load(config: Config, path: String): Map[String, String] = {
        val obj =
          if (StringUtils.isBlank(path)) config.root()
          else config.getObject(path)
        val props = mutable.Map[String, String]()
        make(props, "", obj)
        props.toMap
      }
    }

  implicit val javaMapLoader: ConfigLoader[java.util.Map[String, String]] =
    scalaMapLoader.map(v => v.asJava)

  implicit val pathLoader: ConfigLoader[Path] =
    stringLoader.map(str => Paths.get(str))

  implicit val seqPathLoader: ConfigLoader[Seq[Path]] =
    seqStringLoader.map(strs => strs.map(str => Paths.get(str)))

  implicit val offsetDateTimeLoader: ConfigLoader[OffsetDateTime] =
    stringLoader.map { str =>
      TimeUtils.toOffsetDateTime(str)
    }

  //  implicit def mapLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Map[String, A]] =
  //    new ConfigLoader[Map[String, A]] {
  //      override def load(config: Config, path: String): Map[String, A] = {
  //        val obj = config.getObject(path)
  //        val conf = obj.toConfig
  //        obj.keySet().asScala.map { key =>
  //          key -> valueLoader.load(conf, key)
  //        }.toMap
  //      }
  //    }
}
