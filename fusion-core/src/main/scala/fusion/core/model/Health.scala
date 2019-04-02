package fusion.core.model

trait HealthComponent {

  def name: String = {
    val s   = getClass.getSimpleName
    val str = s.head.toLower + s.tail
    if (str.last == '$') str.init else str
  }

  def health: Health
}

case class Health(status: String, details: Map[String, Any]) {}

object Health {
  val UP   = "UP"
  val DOWN = "DOWN"

  def up(details: Map[String, Any]): Health                        = Health(UP, details)
  def up(): Health                                                 = Health(UP, null)
  def up(detail: (String, Any), details: (String, Any)*): Health   = up(Map(detail) ++ details)
  def down(details: Map[String, Any]): Health                      = Health(DOWN, details)
  def down(): Health                                               = Health(DOWN, null)
  def down(detail: (String, Any), details: (String, Any)*): Health = down(Map(detail) ++ details)
}
