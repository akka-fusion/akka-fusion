package helloscala.common.doc

import helloscala.common.util.Utils
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import scala.collection.immutable
import scala.collection.JavaConverters._

object ExcelUtils {

  def parse(wb: Workbook): immutable.Seq[RichSheet] = {
    Utils.using(wb)(_ =>
      toList(wb).map { sheet =>
        val rows = sheet.rows.map { row =>
          val cells = row.cells.map {
            case c: RichCell => c.toStrictCell
            case c           => c
          }
          row.copy(cells = cells)
        }
        sheet.copy(rows = rows)
      })
  }

  def toList(wb: Workbook): immutable.Seq[RichSheet] = {
    (0 until wb.getNumberOfSheets).map { sheetnum =>
      val sheet = wb.getSheetAt(sheetnum)
      val rows  = toList(sheet)
      RichSheet(sheetnum, rows, sheet)
    }
  }

  def toList(sheet: Sheet): immutable.Seq[RichRow] =
    (sheet.getFirstRowNum until sheet.getLastRowNum).flatMap { rownum =>
      val row = sheet.getRow(rownum)
      if (row.getFirstCellNum < 0) None else Some(RichRow(rownum, toCells(row), row))
    }

  def toCells(row: Row): immutable.Seq[RichCell] =
    row.asScala.map(cell => RichCell(cell.getColumnIndex, cell)).toList

}
