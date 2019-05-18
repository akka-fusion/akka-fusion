package helloscala.common.doc

import java.nio.file.Paths

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

class ExcelUtilsTest extends FunSuite with MustMatchers {

  test("render xlsx") {
    val wb = WorkbookFactory.create(
      Paths.get("/opt/Documents/Work/ihongka/weekly/ZSHK-中层工作周报表【2019.05.06-05.10】-杨景.xlsx").toFile)
    val sheets = ExcelUtils.parse(wb)
    sheets.foreach { sheet =>
      println(s"sheet: $sheet")
      sheet.rows.foreach { row =>
        println(s"row: ${row.num} ${row.mkLine("\t")}")
      }
    }
    sheets must not be empty
  }

}
