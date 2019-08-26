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

import helloscala.common.util.Utils
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import scala.collection.JavaConverters._
import scala.collection.immutable

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
      val rows = toList(sheet)
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
