package helloscala.common.util

import java.util.Objects

object CollectionUtils {
  def isEmpty(coll: Iterable[_]): Boolean              = Objects.isNull(coll) || coll.isEmpty
  def isEmpty(coll: java.util.Collection[_]): Boolean  = Objects.isNull(coll) || coll.isEmpty
  def nonEmpty(coll: Iterable[_]): Boolean             = !isEmpty(coll)
  def nonEmpty(coll: java.util.Collection[_]): Boolean = !isEmpty(coll)
  def isSingle(coll: Iterable[_]): Boolean             = Objects.nonNull(coll) && coll.size == 1
  def isSingle(coll: java.util.Collection[_]): Boolean = Objects.nonNull(coll) && coll.size == 1
}
