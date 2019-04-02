package fusion.http

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCode
import fusion.http.util.HttpUtils

package object server {

  private[server] def jsonEntity(errCode: StatusCode, message: String): (StatusCode, HttpEntity.Strict) =
    errCode -> HttpUtils.entityJson(s"""{"errCode":$errCode,"message":"$message"}""")

}
