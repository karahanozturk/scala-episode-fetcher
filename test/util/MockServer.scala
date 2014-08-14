package util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

object MockServer {
  val server = new WireMockServer(3333)

  def start(): Unit = {
    if (!server.isRunning) server.start()
    WireMock.configureFor("localhost", 3333)
  }

  def reset() {
    WireMock.reset()
  }

  def stop() {
    if (server.isRunning) server.stop()
  }
}
