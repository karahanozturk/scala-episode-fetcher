package service

import javax.inject.Inject

import config.Configuration
import controllers._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Node, NodeSeq, XML}

class EpisodeService @Inject()(httpClient: HttpClient, config: Configuration) {
  val baseUrl = config.getString("nitro.url").get

  def episodes(pid: String): Future[Episode] = {
    httpClient get (baseUrl + "/programmes?pid=" + pid) map {
      case Response(200, body) => parseEpisode(body)
      case Response(404, _) => throw NotFoundException(s"Could not find episode with pid $pid")
      case Response(status, _) => throw InternalServerException(s"Nitro request failed with status code $status")
    }
  }

  private def parseEpisode(body: String): Episode = {

    def parseImage(xml: NodeSeq) = Image((xml \ "@template_url" text).replace("$recipe", "{recipe}"), "image")
    def parseSynopses(xml: NodeSeq): Synopses =  Synopses(xml \ "short" text, xml \ "medium" text, xml \ "long" text)
    def parseReleaseDate(xml: Option[Node]) = xml.map { node =>
      val dateTime = DateTime.parse(node.text, DateTimeFormat.forPattern("yyy-MM-ddZ"))
      dateTime.toString(DateTimeFormat.forPattern("dd MMM yyyy"))
    }

    val xml = XML.loadString(body)

    Episode(
      xml \\ "pid" text,
      parseSynopses(xml \\ "synopses"),
      parseImage(xml \\ "image"),
      xml \\ "episode_of" \\ "@pid" text,
      parseReleaseDate(xml \\ "release_date" headOption)
    )
  }


}
