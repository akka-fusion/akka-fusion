package helloscala.common.jackson

import java.util.Objects

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.exception.HSBadRequestException
import helloscala.common.util.Utils

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.util.control.NonFatal

trait JacksonFactory {
  def defaultObjectMapper: ObjectMapper
}

object Jackson extends JacksonFactory with StrictLogging {
  private lazy val _defaultObjectMapper = createObjectMapper

  def defaultObjectMapper: ObjectMapper = _defaultObjectMapper

  def createObjectNode: ObjectNode = defaultObjectMapper.createObjectNode

  def createArrayNode: ArrayNode = defaultObjectMapper.createArrayNode

  def readTree(text: String): JsonNode = defaultObjectMapper.readTree(text)

  def valueToTree(v: AnyRef): JsonNode = defaultObjectMapper.valueToTree(v)

  def treeToValue[T](tree: TreeNode)(implicit ev1: ClassTag[T]): T =
    defaultObjectMapper.treeToValue(tree, ev1.runtimeClass).asInstanceOf[T]

  def stringify(v: Any): String = defaultObjectMapper.writeValueAsString(v)

  def prettyStringify(v: Any): String = defaultObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(v)

  def extract[T](tree: TreeNode)(implicit ev1: ClassTag[T]): Either[JsonProcessingException, T] = Utils.either {
    defaultObjectMapper.treeToValue(tree, ev1.runtimeClass).asInstanceOf[T]
  }

  @inline def extract[T](compare: Boolean, tree: TreeNode)(implicit ev1: ClassTag[T]): Either[Throwable, T] =
    if (compare) extract(tree)
    else Left(HSBadRequestException(s"compare比较结果为false，需要类型：${ev1.runtimeClass.getName}"))

  def jsonResponseToBO[T](data: AnyRef)(implicit ev1: ClassTag[T]): T = {
    if (Objects.isNull(data)) {
      null.asInstanceOf[T]
    } else {
      val node = Jackson.valueToTree(data)
      Jackson.defaultObjectMapper.treeToValue(node, ev1.runtimeClass).asInstanceOf[T]
    }
  }

  def jsonResponseToList[T](data: AnyRef)(implicit ev1: ClassTag[T]): Seq[T] = {
    val coll: Iterable[_ <: Object] = data match {
      case null                               => Nil
      case list: Iterable[Object]             => list
      case list: java.util.Collection[Object] => list.asScala
      case arr: ArrayNode                     => arr.asScala
      case other                              => Jackson.valueToTree(other).asInstanceOf[ArrayNode].asScala
    }

    coll.map { obj =>
      val node = Jackson.valueToTree(obj)
      Jackson.treeToValue(node)
    }.toList
  }

  private def createObjectMapper: ObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    try {
      val FILTER_ID_CLASS = Class.forName("scalapb.GeneratedMessage")
      mapper
        .setFilterProvider(
          new SimpleFilterProvider().addFilter(
            FILTER_ID_CLASS.getName,
            SimpleBeanPropertyFilter.serializeAllExcept("allFields", "fieldByNumber", "field")))
        .setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
          override def findFilterId(a: Annotated): AnyRef =
            if (FILTER_ID_CLASS.isAssignableFrom(a.getRawType)) FILTER_ID_CLASS.getName else super.findFilterId(a)
        })
    } catch {
      case NonFatal(e) => // do nothing
        logger.warn(s"create ObjectMapper: ${e.toString}", e)
    }

    mapper
      .findAndRegisterModules()
      .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
      .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
      .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
      .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
      .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      //      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      //      .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//      .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    //      .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
    //                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
  }

//  private class MassSerializerProvider(src: SerializerProvider, config: SerializationConfig, f: SerializerFactory)
//      extends DefaultSerializerProvider(src, config, f) {
//    def this() {
//      this(null, null, null)
//    }
//
//    def this(src: MassSerializerProvider) {
//      this(src, null, null)
//    }
//
//    override def copy: DefaultSerializerProvider = {
//      if (getClass ne classOf[MassSerializerProvider]) return super.copy
//      new MassSerializerProvider(this)
//    }
//
//    override def createInstance(config: SerializationConfig, jsf: SerializerFactory) =
//      new MassSerializerProvider(this, config, jsf)
//  }

}
