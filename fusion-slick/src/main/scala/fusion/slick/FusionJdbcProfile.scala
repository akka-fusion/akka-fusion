package fusion.slick

import slick.ast.TypedType
import slick.jdbc.JdbcProfile

trait FusionJdbcProfile extends JdbcProfile {

  trait FusionImplicits {
    this: API =>

    type FilterCriteriaType = Option[Rep[Option[Boolean]]]

    val coalesceString: Seq[Rep[_]] => Rep[String] = SimpleFunction("coalesce")
    val coalesceInt: Seq[Rep[_]] => Rep[Int]       = SimpleFunction("coalesce")
    val coalesceLong: Seq[Rep[_]] => Rep[Long]     = SimpleFunction("coalesce")

    def coalesce[R: TypedType]: Seq[Rep[_]] => Rep[R] = SimpleFunction("coalesce")

    def dynamicFilter(list: Iterable[FilterCriteriaType]): Rep[Option[Boolean]] =
      list
        .collect({ case Some(criteria) => criteria })
        .reduceLeftOption(_ && _)
        .getOrElse(Some(true): Rep[Option[Boolean]])

    def dynamicFilter(item: Option[Rep[Boolean]], list: Option[Rep[Boolean]]*): Rep[Boolean] =
      (item +: list).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])

    def dynamicFilterOr(list: Iterable[FilterCriteriaType]): Rep[Option[Boolean]] =
      list
        .collect({ case Some(criteria) => criteria })
        .reduceLeftOption(_ || _)
        .getOrElse(Some(true): Rep[Option[Boolean]])

    def dynamicFilterOr(item: Option[Rep[Boolean]], list: Option[Rep[Boolean]]*): Rep[Boolean] =
      (item +: list).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ || _).getOrElse(true: Rep[Boolean])
  }

  trait FusionPlainImplicits {}
}

object FusionJdbcProfile extends FusionJdbcProfile
