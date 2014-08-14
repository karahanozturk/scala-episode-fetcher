package service

import javax.inject.Inject

import config.Configuration
import controllers._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, Node, NodeSeq, XML}

class EpisodeService @Inject()(httpClient: HttpClient, config: Configuration) {
  val baseUrl = config.getString("nitro.url").get

  def episodes(pid: String): Future[Option[Episode]] = {
    httpClient get (baseUrl + "/programmes?pid=" + pid) map {
      case Response(200, body) => val bodyXml = XML.loadString(body)
        (bodyXml \\ "@total").text match {
        case "0" => None
        case _ => Some(parseEpisode(bodyXml))
      }
      case Response(status, _) => throw InternalServerException(s"Nitro request failed with status code $status")
    }
  }

  private def parseEpisode(bodyXml: Elem): Episode = {

    def parseImage(xml: NodeSeq) = Image((xml \ "@template_url" text).replace("$recipe", "{recipe}"), "image")
    def parseSynopses(xml: NodeSeq): Synopses = Synopses(xml \ "short" text, xml \ "medium" text, xml \ "long" text)
    def parseReleaseDate(xml: Option[Node]) = xml.map { node =>
      val dateTime = DateTime.parse(node.text, DateTimeFormat.forPattern("yyy-MM-ddZ"))
      dateTime.toString(DateTimeFormat.forPattern("dd MMM yyyy"))
    }

    Episode(
      bodyXml \\ "pid" text,
      parseSynopses(bodyXml \\ "synopses"),
      parseImage(bodyXml \\ "image"),
      bodyXml \\ "episode_of" \\ "@pid" text,
      parseReleaseDate(bodyXml \\ "release_date" headOption)
    )
  }


}
