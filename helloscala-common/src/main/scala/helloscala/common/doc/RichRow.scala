package helloscala.common.doc

import org.apache.poi.ss.usermodel.Row

import scala.collection.immutable

case class RichRow(num: Int, cells: immutable.Seq[ICell], row: Row) {
  def mkLine(sep: String): String = cells.mkString(sep)
}
