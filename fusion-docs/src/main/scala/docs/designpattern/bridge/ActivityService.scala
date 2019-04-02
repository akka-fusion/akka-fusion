package docs.designpattern.bridge

import java.time.LocalDate
import java.time.LocalDateTime

import org.bson.types.ObjectId

case class Activity(id: String, stages: Seq[Activity.Stage] = Nil, dakas: Seq[Daka] = Nil) extends IResource
case class Daka(
    id: String,
    targetId: String,
    userId: String,
    time: LocalDateTime,
    var likes: Seq[DakaLike] = Nil,
    var comment: Seq[Comment] = Nil,
    var comments: Seq[Comment] = Nil)
    extends ActionResource
case class DakaLike(id: String, targetId: String, userId: String, time: LocalDateTime) extends ActionResource
case class Comment(id: String, targetId: String, userId: String, time: LocalDateTime)  extends ActionResource

trait ActivityAction extends Action {
  def activityId: String
}
case class CommentAction(activityId: String, targetId: String, userId: String, time: LocalDateTime)
    extends ActivityAction {
  override def action: String = s"评论动作 $activityId/$targetId"
}
case class DakaCreateAction(activityId: String, targetId: String, userId: String, time: LocalDateTime)
    extends ActivityAction {
  override def action: String = s"打卡动作 $activityId/$targetId"
}
case class DakaLikeAction(activityId: String, targetId: String, userId: String, time: LocalDateTime)
    extends ActivityAction {
  override def action: String = s"点赞打卡 $activityId/$targetId"
}

object Activity {

  object StageType extends Enumeration {
    val DAKA    = Value
    val CHUXUAN = Value
    val FUXUAN  = Value
  }

  case class Stage(begin: LocalDate, end: LocalDate, stageType: StageType.Value) {

    def isValid(date: LocalDateTime): Boolean =
      date.isAfter(begin.atStartOfDay()) && date.isBefore(end.atStartOfDay().plusDays(1))

    def isValid(date: LocalDateTime, stage: StageType.Value): Boolean =
      this.stageType == stage && date.isAfter(begin.atStartOfDay()) && date.isBefore(end.atStartOfDay().plusDays(1))
  }

}

class ActivityService extends BridgeService[Activity, ActivityAction] {
  private var activities = Set[Activity]()

  def insert(payloads: Activity*): Int = {
    activities ++= payloads
    activities.size
  }

  override def like(action: ActivityAction): Either[String, Daka] = {
    for {
      activity <- activities.find(a => a.id == action.activityId).toRight(s"点赞失败：活动不存在。输入: $action。")
      _ <- getStageOrLeft(
        activity.stages,
        action.time,
        Activity.StageType.CHUXUAN,
        leftMsg => s"打卡失败：${leftMsg}输入：$action")
      daka <- activity.dakas.find(v => v.id == action.targetId).toRight(s"打卡不存在，dakaId: ${action.targetId}")
    } yield {
      daka.likes +:= DakaLike(ObjectId.get().toString, action.targetId, action.userId, action.time)
      daka
    }
  }

  override def comment(action: ActivityAction): Either[String, Comment] = {
    for {
      activity <- activities.find(a => a.id == action.activityId).toRight(s"评论失败：活动不存在。输入: $action。")
      _ <- getStageOrLeft(
        activity.stages,
        action.time,
        Activity.StageType.FUXUAN,
        leftMsg => s"评论失败：${leftMsg}输入：$action")
      daka <- activity.dakas.find(v => v.id == action.targetId).toRight(s"评论失败：打卡不存在。输入: $action")
    } yield {
      val comment = Comment(ObjectId.get().toString, daka.id, action.userId, action.time)
      daka.comments +:= comment
      comment
    }
  }

  def getStageOrLeft(
      stages: Seq[Activity.Stage],
      date: LocalDateTime,
      stageEnum: Activity.StageType.Value,
      leftMsg: String => String): Either[String, Activity.Stage] =
    stages.find(_.isValid(date)) match {
      case Some(stage) if stage.stageType == stageEnum => Right(stage)
      case Some(stage)                                 => Left(leftMsg(s"活动阶段无效，输入时间：$date 不匹配stage: $stage。"))
      case _                                           => Left(leftMsg(s"活动阶段无效，输入时间：$date。"))
    }

}
