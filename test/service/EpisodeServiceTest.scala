package service

import config.Configuration
import controllers._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.xml.XML

class EpisodeServiceTest extends Specification with Mockito {
  val config = mock[Configuration]
  val httpClient = mock[HttpClient]

  config.getString("nitro.url") returns Some("http://some.url.com")
  httpClient.get("http://some.url.com/programmes?pid=pid") returns Future.successful(
    Response(200, XML.loadFile("test/resources/nitro_episodes.xml").toString()))

  val service = new EpisodeService(httpClient, config)

  "Episode Service" should {

    "should parse and build episodes from client response" in {
      val episodesFuture = service.episodes("pid")
      val episode = Await.result(episodesFuture, 1000 milli).get

      episode.pid must equalTo("p00lfrb3")
      episode.synopses must equalTo(Synopses("Short Synopsis", "Medium Synopsis", "Long Synopsis"))
      episode.image must equalTo(Image("http://ichef.bbci.co.uk/images/ic/{recipe}/p01h7ms3.jpg", "image"))
      episode.releaseDate must equalTo (Some("12 Nov 1972"))
      episode.parentId must equalTo("b00ffbjg")
    }

    "should return None when response does not have an episode" in {
      httpClient.get(any[String]) returns Future.successful(
        Response(200, XML.loadFile("test/resources/nitro_not_found_episode.xml").toString()))

      val episodesFuture: Future[Option[Episode]] = service.episodes("pid")
      Await.result(episodesFuture, 100 milli) must equalTo(None)
    }

    "should throw InternalServerException when any other status is returned" in {
      httpClient.get(any[String]) returns Future.successful(Response(500, "not found"))

      val episodesFuture: Future[Option[Episode]] = service.episodes("pid")
      Await.result(episodesFuture, 100 milli) must throwA[InternalServerException]
    }
  }
}
