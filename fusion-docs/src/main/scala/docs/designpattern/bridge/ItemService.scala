package docs.designpattern.bridge

import java.time.LocalDateTime

import org.bson.types.ObjectId

case class Item(id: String, var likes: Seq[ItemLike] = Nil) extends IResource

case class ItemLike(id: String, targetId: String, userId: String, time: LocalDateTime) extends ActionResource

case class ItemLikeAction(targetId: String, userId: String, time: LocalDateTime) extends Action {
  override def action: String = s"点赞节目 $targetId"
}

class ItemService extends BridgeService[Item, Action] {
  private var items = Set[Item]()

  override def insert(payloads: Item*): Int = {
    items ++= payloads
    items.size
  }

  /**
   * 点赞
   *
   * @param action 动作
   * @return 成功返回动力本身
   */
  override def like(action: Action): Either[String, ActionResource] =
    for {
      item <- items.find(item => item.id == action.targetId).toRight(s"节点不存在，action: $action")
    } yield {
      val like = ItemLike(ObjectId.get().toString, action.targetId, action.userId, action.time)
      item.likes +:= like
      like
    }

  override def comment(action: Action): Either[String, ActionResource] = ???
}
