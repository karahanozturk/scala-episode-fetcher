package prefetcher

import javax.inject.Inject

import config.Configuration
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, Node, NodeSeq, XML}

case class InternalServerException(message: String) extends Throwable(message)

case class NotFoundException(message: String) extends Throwable

class EpisodeFetcher @Inject()(httpClient: HttpClient, config: Configuration) {
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

  def generateTitle(n: NitroTitles): Title = n match {
    case NitroTitles(episode, Some(brand), None, None, _) => Title(brand, Some(episode))
    case NitroTitles(episode, Some(brand), _, Some(subSeries), _) => Title(brand, Some(subSeries + n.episodeWithPrefix))
    case NitroTitles(episode, Some(brand), Some(series), None, _) => Title(brand, Some(series + n.episodeWithPrefix))
    case NitroTitles(episode, None, Some(series), Some(subSeries), _) => Title(series, Some(subSeries + n.episodeWithPrefix))
    case NitroTitles(episode, None, Some(series), None, Some(position)) => Title(series, Some(s"$position. $episode"))
    case NitroTitles(episode, None, Some(series), None, None) => Title(series, Some(episode))
    case NitroTitles(episode, None, None, Some(subSeries), _) => Title(subSeries, Some(episode))
    case NitroTitles(episode, None, None, None, None) => Title(episode, None)
  }
}

case class NitroTitles(episode: String, brand: Option[String], series: Option[String], subSeries: Option[String], position: Option[Int]) {
  def episodeWithPrefix = position match {
    case Some(pos) => s": $pos. $episode"
    case None => s": $episode"
  }
}
