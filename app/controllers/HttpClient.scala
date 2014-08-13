package controllers

import scala.concurrent.Future

case class Response(status: Int, body: String)

class HttpClient {
  def get(s: String): Future[Response] = ???

}
