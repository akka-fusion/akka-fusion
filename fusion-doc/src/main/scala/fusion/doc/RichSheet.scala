package fusion.doc

import org.apache.poi.ss.usermodel.Sheet

import scala.collection.immutable

case class RichSheet(num: Int, rows: immutable.Seq[RichRow], sheet: Sheet) {
  def sheetName: String         = sheet.getSheetName
  override def toString: String = s"${getClass.getSimpleName}($num,$sheetName)"
}
