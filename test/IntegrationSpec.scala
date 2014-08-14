import com.github.tomakehurst.wiremock.client.WireMock._
import controllers.{Image, Synopses, HttpClient}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test._
import util.MockServer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.XML

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {
  implicit val formats = DefaultFormats
  val httpClient = new HttpClient
  val app = FakeApplication(additionalConfiguration = Map("nitro.url" -> "http://localhost:3333"))

  MockServer.start()

  "Episode Fetcher" should {
    "given an episode exists in Nitro" in {
      MockServer.reset()
      stubFor(get(urlEqualTo("/programmes?pid=p00lfrb3")).
        willReturn(aResponse().
        withStatus(200).
        withBody(XML.loadFile("test/resources/nitro_episodes.xml").toString())))

      "returns the iBL episode" in new WithServer(app) {
        val result = Await.result(httpClient.get(s"http://localhost:$port/episodes?pid=p00lfrb3"), 1000 milli)
        result.status mustEqual 200
        val json = parse(result.body)

        (json \ "pid").extract[String] mustEqual "p00lfrb3"
        (json \ "synopses").extract[Synopses] mustEqual Synopses("Short Synopsis", "Medium Synopsis", "Long Synopsis")
        (json \ "image").extract[Image] mustEqual Image("http://ichef.bbci.co.uk/images/ic/{recipe}/p01h7ms3.jpg", "image")
        (json \ "parent_id").extract[String] mustEqual "b00ffbjg"
      }
    }

//    "given an episode doesn't exist in Nitro" in {
//      "returns a not found response" in {
//
//      }
//    }
  }
}
