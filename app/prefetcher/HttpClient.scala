package prefetcher

import dispatch.{Http, url}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class Response(status: Int, body: String)

class HttpClient {
  def get(urlString: String): Future[Response] = {
    Http(url(urlString).GET).map(response => Response(response.getStatusCode, response.getResponseBody))
  }
}
