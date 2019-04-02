package docs.designpattern.bridge

import java.time.LocalDate
import java.time.LocalDateTime

import org.scalatest.BeforeAndAfterAll
import org.scalatest.EitherValues
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

class BridgeServiceFactoryTest extends FunSuite with MustMatchers with EitherValues with BeforeAndAfterAll {
  test("activity-daka result include Left(活动阶段无效)") {
    val activityService = BridgeServiceFactory.getService[ActivityService](BridgeServiceFactory.ActivityType)
    val dakaLikeAction  = DakaLikeAction("activity1", "daka1", "userId", LocalDate.of(2019, 3, 26).atTime(12, 0))
    activityService.like(dakaLikeAction).left.value must include("活动阶段无效")
  }

  test("activity-daka result include Left(活动不存在)") {
    val activityService = BridgeServiceFactory.getService[ActivityService](BridgeServiceFactory.ActivityType)
    val dakaLikeAction  = DakaLikeAction("activity2", "daka1", "userId", LocalDate.of(2019, 3, 26).atTime(12, 0))
    activityService.like(dakaLikeAction).left.value must include("活动不存在")
  }

  test("activity-daka result is Right") {
    val activityService = BridgeServiceFactory.getService[ActivityService](BridgeServiceFactory.ActivityType)
    val dakaLikeAction  = DakaLikeAction("activity1", "daka1", "userId", LocalDate.of(2019, 4, 12).atTime(12, 0))
    val result          = activityService.like(dakaLikeAction)
    println(result)
    result.isRight must be(true)
  }

  test("bridge result is Right") {
    val itemService =
      BridgeServiceFactory.getService[ActivityService](BridgeServiceFactory.ItemType).asInstanceOf[ItemService]
    val itemAction = ItemLikeAction("item1", "userId", LocalDate.of(2019, 3, 26).atTime(12, 0))
    itemService.like(itemAction).isRight must be(true)
  }

  override protected def beforeAll(): Unit = {
    BridgeServiceFactory
      .getService[ActivityService](BridgeServiceFactory.ActivityType)
      .insert(Activity(
        "activity1",
        stages = Seq(
          Activity.Stage(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 7), Activity.StageType.DAKA),
          Activity.Stage(LocalDate.of(2019, 4, 8), LocalDate.of(2019, 4, 14), Activity.StageType.CHUXUAN),
          Activity.Stage(LocalDate.of(2019, 4, 15), LocalDate.of(2019, 4, 21), Activity.StageType.FUXUAN)),
        dakas = Seq(Daka("daka1", "activity1", "user1", LocalDateTime.now()))))

    BridgeServiceFactory.getService[ItemService](BridgeServiceFactory.ItemType).insert(Item("item1"))
  }

}
