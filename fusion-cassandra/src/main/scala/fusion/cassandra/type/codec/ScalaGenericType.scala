package fusion.cassandra.`type`.codec

import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeParameter
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken
import edu.umd.cs.findbugs.annotations.NonNull

class ScalaTypeParameter[T] extends TypeParameter[T]() {}

object ScalaGenericType {

  @NonNull def seqOf[T](@NonNull elementType: GenericType[T]): GenericType[Seq[T]] = {
    val token: TypeToken[Seq[T]] =
      new TypeToken[Seq[T]]() {}.where(new ScalaTypeParameter[T](), elementType.__getToken())
    GenericType.of(token.getType).asInstanceOf[GenericType[Seq[T]]]
  }
}
