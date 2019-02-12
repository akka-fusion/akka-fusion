package fusion.http.util

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

object HttpUtils {

  def entityJson(string: String) = HttpEntity(ContentTypes.`application/json`, string)

}
