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

package fusion.doc

import java.nio.file.Paths

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ExcelUtilsTest extends AnyFunSuite with Matchers {
  test("render xlsx") {
    val wb = WorkbookFactory.create(
      Paths.get("/opt/Documents/Work/akka-fusion/weekly/ZSHK-中层工作周报表【2019.05.06-05.10】-杨景.xlsx").toFile
    )
    val sheets = ExcelUtils.parse(wb)
    sheets.foreach { sheet =>
      println(s"sheet: $sheet")
      sheet.rows.foreach { row =>
        println(s"row: ${row.num} ${row.mkLine("\t")}")
      }
    }
    sheets should not be empty
  }
}
