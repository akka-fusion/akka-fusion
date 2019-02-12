package fusion.http.server
import akka.http.scaladsl.server.{Directives, Route}

trait AbstractRoute extends Directives with HttpDirectives {

  def route: Route

}
