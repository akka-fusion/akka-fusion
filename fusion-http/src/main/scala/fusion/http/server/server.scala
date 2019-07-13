package fusion.http

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCode
import fusion.http.util.HttpUtils

package object server {

  private[server] def jsonEntity(status: StatusCode, msg: String): (StatusCode, HttpEntity.Strict) =
    status -> HttpUtils.entityJson(s"""{"status":${status.intValue()},"msg":"$msg"}""")

  private[server] def jsonResponse(status: StatusCode, msg: String): HttpResponse =
    HttpResponse(status, entity = HttpUtils.entityJson(s"""{"status":${status.intValue()},"msg":"$msg"}"""))

}
