package helloscala.common.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.{JsonParser, JsonProcessingException, TreeNode}
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.fasterxml.jackson.databind.ser.{DefaultSerializerProvider, SerializerFactory}
import com.fasterxml.jackson.databind._
import helloscala.common.util.Utils

import scala.reflect.ClassTag

object Jackson {
  val defaultObjectMapper: ObjectMapper = createObjectMapper

  def createObjectNode: ObjectNode = defaultObjectMapper.createObjectNode

  def createArrayNode: ArrayNode = defaultObjectMapper.createArrayNode

  def readTree(jstr: String): JsonNode = defaultObjectMapper.readTree(jstr)

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
    else Left(new IllegalArgumentException(s"compare比较结果为false，需要类型：${ev1.runtimeClass.getName}"))

//  private val FILTER_ID_CLASS: Class[akka.protobuf.GeneratedMessage] = classOf[akka.protobuf.GeneratedMessage]

  private def createObjectMapper: ObjectMapper = {
    new ObjectMapper()
//      .setFilterProvider(new SimpleFilterProvider()
//        .addFilter(FILTER_ID_CLASS.getName, SimpleBeanPropertyFilter.serializeAllExcept("allFields")))
//      .setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
//        override def findFilterId(a: Annotated): AnyRef =
//          if (FILTER_ID_CLASS.isAssignableFrom(a.getRawType)) FILTER_ID_CLASS.getName else super.findFilterId(a)
//      })
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
      .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    //      .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
    //                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
  }

  private class MassSerializerProvider(src: SerializerProvider, config: SerializationConfig, f: SerializerFactory)
      extends DefaultSerializerProvider(src, config, f) {
    def this() {
      this(null, null, null)
    }

    def this(src: MassSerializerProvider) {
      this(src, null, null)
    }

    override def copy: DefaultSerializerProvider = {
      if (getClass ne classOf[MassSerializerProvider]) return super.copy
      new MassSerializerProvider(this)
    }

    override def createInstance(config: SerializationConfig, jsf: SerializerFactory) =
      new MassSerializerProvider(this, config, jsf)
  }

}
