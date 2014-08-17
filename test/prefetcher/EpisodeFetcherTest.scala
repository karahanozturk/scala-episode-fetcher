package prefetcher

import config.Configuration
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.xml.XML

class EpisodeFetcherTest extends Specification with Mockito {
  val config = mock[Configuration]
  val httpClient = mock[HttpClient]

  config.getString("nitro.url") returns Some("http://some.url.com")
  httpClient.get("http://some.url.com/programmes?pid=pid") returns Future.successful(
    Response(200, XML.loadFile("test/resources/nitro_episodes.xml").toString()))

  val fetcher = new EpisodeFetcher(httpClient, config)

  "Episode Service" should {

    "parse and build episodes from client response" in {
      val episodesFuture = fetcher.episodes("pid")
      val episode = Await.result(episodesFuture, 1000 milli).get

      episode.pid must equalTo("p00lfrb3")
      episode.synopses must equalTo(Synopses("Short Synopsis", "Medium Synopsis", "Long Synopsis"))
      episode.image must equalTo(Image("http://ichef.bbci.co.uk/images/ic/{recipe}/p01h7ms3.jpg", "image"))
      episode.releaseDate must equalTo(Some("12 Nov 1972"))
      episode.parentId must equalTo("b00ffbjg")
    }

    "return None when response does not have an episode" in {
      httpClient.get(any[String]) returns Future.successful(
        Response(200, XML.loadFile("test/resources/nitro_not_found_episode.xml").toString()))

      val episodesFuture: Future[Option[Episode]] = fetcher.episodes("pid")
      Await.result(episodesFuture, 100 milli) must equalTo(None)
    }

    "throw InternalServerException when any other status is returned" in {
      httpClient.get(any[String]) returns Future.successful(Response(500, "not found"))

      val episodesFuture: Future[Option[Episode]] = fetcher.episodes("pid")
      Await.result(episodesFuture, 100 milli) must throwA[InternalServerException]
    }
  }

  "When only an episode title is present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", None, None, None, None)
    val titles = fetcher.generateTitle(nitroTitles)
    "have the episode title as the title" in {titles.title  must equalTo("Episode") }
    "and an empty subtitle" in {titles.subtitle must equalTo(None) }
  }

  "When subseries and episode titles are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", None, None, Some("Subseries"), None)
    val titles = fetcher.generateTitle(nitroTitles)
    "have the subseries as the title" in {titles.title  must equalTo("Subseries") }
    "and the episode title as subtitle" in {titles.subtitle.get must equalTo("Episode") }
  }

  "When brand and episode titles are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", Some("Brand"), None, None, None)
    val titles = fetcher.generateTitle(nitroTitles)
    "have the brand title as the title" in {titles.title  must equalTo("Brand") }
    "and the episode title as subtitle" in {titles.subtitle.get must equalTo("Episode") }
  }

  "When brand title, episode title and positions are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", Some("Brand"), None, None, Some(3))
    val titles = fetcher.generateTitle(nitroTitles)
    "have the brand title as the title" in {titles.title  must equalTo("Brand") }
    "and the episode title as subtitle" in {titles.subtitle.get must equalTo("Episode") }
  }

  "When brand, series and episode titles are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", Some("Brand"), Some("Series"), None, None)
    val titles = fetcher.generateTitle(nitroTitles)
    "have the brand title as the title" in {titles.title  must equalTo("Brand") }
    "and series and episode titles as subtitle" in {titles.subtitle.get must equalTo("Series: Episode") }
  }

  "When brand, series, episode titles and position are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", Some("Brand"), Some("Series"), None, Some(3))
    val titles = fetcher.generateTitle(nitroTitles)
    "have the brand title as the title" in {titles.title  must equalTo("Brand") }
    "and the position, series and episode titles as the subtitle" in {titles.subtitle.get must equalTo("Series: 3. Episode") }
  }

  "When brand, series, subseries and episode titles are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", Some("Brand"), Some("Series"), Some("Subseries"), None)
    val titles = fetcher.generateTitle(nitroTitles)
    "have the brand title as the title" in {titles.title  must equalTo("Brand") }
    "and subseries and episode titles as the subtitle" in {titles.subtitle.get must equalTo("Subseries: Episode") }
  }

  "When brand, series, subseries, episode titles and position are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", Some("Brand"), Some("Series"), Some("Subseries"), Some(3))
    val titles = fetcher.generateTitle(nitroTitles)
    "have the brand title as the title" in {titles.title  must equalTo("Brand") }
    "and the position, subseries and episode titles as the subtitle" in {titles.subtitle.get must equalTo("Subseries: 3. Episode") }
  }

  "When series and episode titles are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", None, Some("Series"), None, None)
    val titles = fetcher.generateTitle(nitroTitles)
    "have the series title as the title" in {titles.title  must equalTo("Series") }
    "and the episode title as the subtitle" in {titles.subtitle.get must equalTo("Episode") }
  }

  "When series, episode titles and position are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", None, Some("Series"), None, Some(3))
    val titles = fetcher.generateTitle(nitroTitles)
    "have the series title as the title" in {titles.title  must equalTo("Series") }
    "and the position and episode title as the subtitle" in {titles.subtitle.get must equalTo("3. Episode") }
  }

  "When series, subseries and episode titles are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", None, Some("Series"), Some("Subseries"), None)
    val titles = fetcher.generateTitle(nitroTitles)
    "have the series title as the title" in {titles.title  must equalTo("Series") }
    "and the subseries and episode title as the subtitle" in {titles.subtitle.get must equalTo("Subseries: Episode") }
  }

  "When position, series, subseries and episode titles are present, \n Titles" should {
    val nitroTitles = NitroTitles("Episode", None, Some("Series"), Some("Subseries"), Some(3))
    val titles = fetcher.generateTitle(nitroTitles)
    "have the series title as the title" in {titles.title  must equalTo("Series") }
    "and the subseries, position and episode as the subtitle" in {titles.subtitle.get must equalTo("Subseries: 3. Episode") }
  }
}
