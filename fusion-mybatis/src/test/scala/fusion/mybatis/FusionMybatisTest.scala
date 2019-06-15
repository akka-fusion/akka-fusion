package fusion.mybatis

import java.sql.SQLException

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fusion.mybatis.mapper.FileMapper
import fusion.mybatis.model.CFile
import fusion.test.FusionTestFunSuite

// #FusionMybatisTest
class FusionMybatisTest extends TestKit(ActorSystem("fusion-mybatis")) with FusionTestFunSuite {

  test("testSqlSession") {
    val sqlSessionFactory = FusionMybatis(system).component
    sqlSessionFactory must not be null

    // auto commit is false
    val session = sqlSessionFactory.openSession()
    try {
      session must not be null
    } finally {
      session.close()
    }
  }

  test("file insert") {
    val sqlSessionFactory = FusionMybatis(system).component

    // using函数将自动提交/回滚（异常抛出时）
    sqlSessionFactory.using { session =>
      val fileMapper = session.getMapper(classOf[FileMapper])
      val file       = CFile("file_id", "文件", "/32/234242.jpg", 98234)
      fileMapper.insert(file)
//      session.commit()
      throw new SQLException()
    }
  }

  test("file list") {
    val sqlSessionFactory = FusionMybatis(system).component
    sqlSessionFactory.using { session =>
      val fileMapper = session.getMapper(classOf[FileMapper])
      val list       = fileMapper.list(10)
      list.forEach(println)
      list must not be empty
    }
  }

}
// #FusionMybatisTest
