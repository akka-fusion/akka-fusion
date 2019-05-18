package helloscala.common.doc

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType

trait ICell {
  def num: Int
  def value: Any
}

case class StrictCell(num: Int, value: Any, cellType: CellType) extends ICell {
  def toText(): String = toText("")

  def toText(deft: String): String = cellType match {
    case CellType.BOOLEAN => value.toString
    case CellType.NUMERIC => value.toString
    case CellType.STRING  => value.asInstanceOf[String]
    case CellType.ERROR   => deft
    case _                => deft
  }
}

case class RichCell(num: Int, cell: Cell) extends ICell {
  def cellType: CellType = cell.getCellType

  def toStrictCell = StrictCell(num, value, cell.getCellType)

  def value: Any = cell.getCellType match {
    case CellType.BOOLEAN => cell.getBooleanCellValue
    case CellType.NUMERIC => cell.getNumericCellValue
    case CellType.FORMULA =>
      cell.getCachedFormulaResultType match {
        case CellType.BOOLEAN => cell.getBooleanCellValue
        case CellType.NUMERIC => cell.getNumericCellValue
        case CellType.STRING  => cell.getStringCellValue
        case CellType.ERROR   => cell.getErrorCellValue
      }
    case CellType.STRING => cell.getStringCellValue
    case CellType.ERROR  => cell.getErrorCellValue
    case _               => ""
  }

  def toText(): String = toText("")

  def toText(deft: String): String = cell.getCellType match {
    case CellType.BOOLEAN => cell.getBooleanCellValue.toString
    case CellType.NUMERIC => cell.getNumericCellValue.toString
    case CellType.FORMULA =>
      cell.getCachedFormulaResultType match {
        case CellType.BOOLEAN => cell.getBooleanCellValue.toString
        case CellType.NUMERIC => cell.getNumericCellValue.toString
        case CellType.STRING  => cell.getStringCellValue
        case CellType.ERROR   => deft
      }
    case CellType.STRING => cell.getStringCellValue
    case CellType.ERROR  => deft
    case _               => deft
  }

}
