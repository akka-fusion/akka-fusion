/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.mybatis

import java.util.function.Consumer

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
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
    sqlSessionFactory.transactional { session =>
      val fileMapper = session.getMapper(classOf[FileMapper])
      val file = CFile("file_id", "文件", "/32/234242.jpg", 98234)
      fileMapper.insert(file)
//      session.commit()
//      throw new SQLException()
    }
  }

  test("file list") {
    val sqlSessionFactory = FusionMybatis(system).component
    sqlSessionFactory.transactional { session =>
      val fileMapper = session.getMapper(classOf[FileMapper])
      val list = fileMapper.list(10)
      list.forEach(new Consumer[CFile] {
        override def accept(t: CFile): Unit = println(t)
      })
      list must not be empty
    }
  }

  test("file page") {
    val sqlSessionFactory = FusionMybatis(system).component
//    val result = sqlSessionFactory.transactional { session =>
//      val fileMapper = session.getMapper(classOf[FileMapper])
//      val req        = new Page[CFile](0, 10)
//      fileMapper.selectPage(req, null)
//    }
    val result = sqlSessionFactory.mapperTransactional[FileMapper, IPage[CFile]] { fileMapper =>
      val req = new Page[CFile](0, 10)
      fileMapper.selectPage(req, null)
    }
    result.getRecords must not be empty
    result.getRecords.forEach(new Consumer[CFile] {
      override def accept(t: CFile): Unit = println(t)
    })
    println(result)
  }

}
// #FusionMybatisTest
