package fusion.mybatis

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fusion.mybatis.mapper.FileMapper
import fusion.test.FusionTestFunSuite
import helloscala.common.util.Utils

// #FusionMybatisTest
class FusionMybatisTest extends TestKit(ActorSystem("fusion-mybatis")) with FusionTestFunSuite {

  test("testSqlSession") {
    val sqlSessionFactory = FusionMybatis(system).component
    sqlSessionFactory must not be null

    Utils.using(sqlSessionFactory.openSession()) { session =>
      session must not be null
    }
  }

  test("testFileMapper") {
    val sqlSessionFactory = FusionMybatis(system).component
    Utils.using(sqlSessionFactory.openSession()) { session =>
      val fileMapper = session.getMapper(classOf[FileMapper])
      val list = fileMapper.list(10)
      list.forEach(println)
      list must not be empty
    }
  }

}
// #FusionMybatisTest
