package controllers

import scala.concurrent.Future
import dispatch._
import play.api.libs.concurrent.Execution.Implicits._

case class Response(status: Int, body: String)

class HttpClient {
  def get(urlString: String): Future[Response] = {
    Http(url(urlString).GET).map(response => Response(response.getStatusCode, response.getResponseBody))
  }
}
