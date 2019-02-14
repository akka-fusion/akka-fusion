package fusion.data.mongodb

import org.bson.types.ObjectId

package object jackson {
  private[jackson] val OBJECT_ID = classOf[ObjectId]
}
