package controllers

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.FakeRequest
import play.api.test.Helpers._
import prefetcher.{Episode, EpisodeFetcher, Image, Synopses}

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class EpisodeControllerTest extends Specification with Mockito {
  implicit val formats = DefaultFormats + JsonUtils.episodeSerializer

  val expectedEpisode = Episode("pid", Synopses("Small", "Medium", "Large"), Image("url", "image"), "parent_pid", None, "title", None)
  val episodeFuture = Future.successful(Some(expectedEpisode))
  val service = mock[EpisodeFetcher]
  service.fetch("pid") returns episodeFuture

  val controller = new EpisodeController(service)

  "Episode Controller" should {

    "return Successful with episode response when episode is found" in {
      val result = controller.episodes("pid")(FakeRequest(GET, "/episodes"))
      status(result) must equalTo(OK)
      val actualEpisode = parse(contentAsString(result)).extract[Episode]
      actualEpisode must equalTo(expectedEpisode)
    }

    "return Not Found response when episode does not exist" in {
      service.fetch("pid") returns Future.successful(None)
      val result = controller.episodes("pid")(FakeRequest(GET, "/episodes"))
      status(result) must equalTo(NOT_FOUND)
    }
  }
}
