package features.steps

import com.github.tomakehurst.wiremock.client.WireMock._
import cucumber.api.DataTable
import cucumber.api.scala.{EN, ScalaDsl}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, _}
import org.specs2.matcher.ShouldMatchers
import play.api.test.{FakeApplication, TestServer}
import prefetcher.{HttpClient, Image, Response, Synopses}
import util.MockServer

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.XML

class FetcherSteps extends ScalaDsl with EN with ShouldMatchers {

  implicit val formats = DefaultFormats
  val httpClient = new HttpClient
  val port = 9090
  val app = FakeApplication(additionalConfiguration = Map("nitro.url" -> "http://localhost:3333"))
  lazy val server = TestServer(port, app)
  var result: Option[Response] = None

  Before() { scenario =>
    MockServer.start()
    MockServer.reset()
    server.start()
  }

  Given( """^an episode with pid "(.*?)" exists in Nitro$""") { (pid: String) =>
    stubFor(get(urlEqualTo(s"/programmes?pid=$pid")).
      willReturn(aResponse().
      withStatus(200).
      withBody(XML.loadFile("test/resources/nitro_episodes.xml").toString())))
  }

  Given( """^an episode with pid "(.*?)" doesn't exists in Nitro$""") { (pid: String) =>
    stubFor(get(urlEqualTo(s"/programmes?pid=$pid")).
      willReturn(aResponse().
      withStatus(200).
      withBody(XML.loadFile("test/resources/nitro_not_found_episode.xml").toString())))
  }

  When( """^I request IBL episode "(.*?)"$""") { (pid: String) =>
    result = Some(Await.result(httpClient.get(s"http://localhost:$port/episodes?pid=p00lfrb3"), 1 second))
  }

  Then( """^it returns (\d+) response$""") { (status: Int) =>
    result.get.status should equalTo(status)
  }

  Then( """^the episode has the expected attributes$""") { () =>
    val json = parse(result.get.body)
    (json \ "synopses").extract[Synopses] should be(Synopses("Short Synopsis", "Medium Synopsis", "Long Synopsis"))
    (json \ "image").extract[Image] should be(Image("http://ichef.bbci.co.uk/images/ic/{recipe}/p01h7ms3.jpg", "image"))
  }

  And( """^the episode has the following attributes:$""") { (data: DataTable) =>
    assertWithDataMatrix(parse(result.get.body), data)
  }

  And( """^the following titles:$""") { (data: DataTable) =>
    assertWithDataMatrix(parse(result.get.body), data)
  }

  And( """^the following synopses:$""") { (data: DataTable) =>
    assertWithDataMatrix(parse(result.get.body) \ "synopses", data)
  }

  And( """^the following image attributes:$""") { (data: DataTable) =>
    assertWithDataMatrix(parse(result.get.body) \ "image", data)
  }

  def assertWithDataMatrix(json: JValue, data: DataTable) {
    val listOfMaps = data.asMaps(classOf[String], classOf[String]).asScala
    listOfMaps.foreach(keyValueMap =>
      keyValueMap.asScala.foreach(keyVal =>
        (json \ keyVal._1).extract[String] should be(keyVal._2)))
  }


  def assertJson(keyVal: (String, String)): scala.Unit = {
    val json = parse(result.get.body)
    (json \ keyVal._1).toString should be(keyVal._2)
  }

  After() { scenario =>
    MockServer.stop()
    server.stop()
  }


}
