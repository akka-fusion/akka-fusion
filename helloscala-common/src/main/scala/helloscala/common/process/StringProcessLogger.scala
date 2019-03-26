package helloscala.common.process

import scala.sys.process.ProcessLogger

class StringProcessLogger extends ProcessLogger {
  private var _outputs = Vector.empty[String]
  private var _errputs = Vector.empty[String]

  def outputs: Vector[String] = _outputs
  def errputs: Vector[String] = _errputs

  def outOrErrString: String = if (outputs.isEmpty) errputs.mkString("\n") else outputs.mkString("\n")
  def outStringAll: String   = outputs.mkString("\n") + "\n\n" + errputs.mkString("\n")

  override def out(s: => String): Unit = {
    _outputs :+= s
  }

  override def err(s: => String): Unit = {
    _errputs :+= s
  }

  override def buffer[T](f: => T): T = f
}
