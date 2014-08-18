package features.steps

import com.github.tomakehurst.wiremock.client.WireMock._
import cucumber.api.DataTable
import cucumber.api.scala.{EN, ScalaDsl}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, _}
import org.scalatest.matchers.ShouldMatchers
import play.api.test.{FakeApplication, TestServer}
import prefetcher.{HttpClient, Response, Synopses, Image}
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
    result.get.status should be(status)
  }

  Then( """^the episode has the expected attributes$""") { () =>
    val json = parse(result.get.body)

    println(json)

    (json \ "pid").extract[String] should be("p00lfrb3")
    (json \ "synopses").extract[Synopses] should be(Synopses("Short Synopsis", "Medium Synopsis", "Long Synopsis"))
    (json \ "image").extract[Image] should be(Image("http://ichef.bbci.co.uk/images/ic/{recipe}/p01h7ms3.jpg", "image"))
    (json \ "parent_id").extract[String] should be("b00ffbjg")
    (json \ "release_date").extract[String] should be("12 Nov 1972")
    (json \ "title").extract[String] should be("D-Day 70")
    (json \ "subtitle").extract[String] should be("The Heroes Return: 1. The First Impact")
  }

  And( """^the episode has the following attributes:$""") { (data: DataTable) =>
    val json = parse(result.get.body)
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
