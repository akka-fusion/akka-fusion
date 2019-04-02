package docs.designpattern.bridge

import java.time.LocalDateTime

/**
 * 资源接口
 * subclass: Activity, Daka, Comment
 */
trait IResource {
  // 资源ID
  def id: String
}

/**
 * 各操作生成的结果
 */
trait ActionResource extends IResource {
  // 操作结果对应的资源ID
  def targetId: String
  // 操作用户ID
  def userId: String
  // 操作时间
  def time: LocalDateTime
}

/**
 * 资源动作
 */
trait Action {
  // 资源目标ID
  def targetId: String
  // 用户ID
  def userId: String
  // 运行执行时间
  def time: LocalDateTime
  // 执行的具体动作
  def action: String
}

/**
 * 资源服务
 */
trait BridgeService[ResourceType <: IResource, ActionType <: Action] {

  /**
   * 添加资源
   *
   * @param resource 资源
   * @return 插入成功返回当前资源数据，失败返回-1
   */
  def insert(resource: ResourceType*): Int

  /**
   * 点赞
   *
   * @param action 点赞动作
   * @return 成功返回生成的动作资源，失败返回错误描述
   */
  def like(action: ActionType): Either[String, ActionResource]

  /**
   * 评论
   *
   * @param action 评论动作
   * @return 成功返回生成的动作资源，失败返回错误描述
   */
  def comment(action: ActionType): Either[String, ActionResource]
}

/**
 * 工厂
 */
object BridgeServiceFactory {
  sealed trait FactoryType
  case object ActivityType extends FactoryType
  case object ItemType     extends FactoryType

  private val activityManager = new ActivityService()
  private val itemManager     = new ItemService()

  def getService[T <: BridgeService[_, _]](typ: FactoryType): T = typ match {
    case ActivityType => activityManager.asInstanceOf[T]
    case ItemType     => itemManager.asInstanceOf[T]
  }
}
