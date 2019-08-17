package fusion.slick

import slick.jdbc.JdbcProfile

trait SlickSupport[P <: JdbcProfile] {
  val db: P#API#Database
}
