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
      case Response(200, body) =>
        val bodyXml = XML.loadString(body)
        (bodyXml \\ "@total").text match {
          case "0" => None
          case _ => Some(parseEpisode(bodyXml))
        }
      case Response(status, _) => throw InternalServerException(s"Nitro request failed with status code $status")
    }
  }

  implicit def generateTitle(n: NitroTitles) = n match {
    case NitroTitles(episode, Some(brand), None, None, _) => Titles(brand, Some(episode))
    case NitroTitles(episode, Some(brand), _, Some(subSeries), _) => Titles(brand, Some(subSeries + n.episodeWithPrefix))
    case NitroTitles(episode, Some(brand), Some(series), None, _) => Titles(brand, Some(series + n.episodeWithPrefix))
    case NitroTitles(episode, None, Some(series), Some(subSeries), _) => Titles(series, Some(subSeries + n.episodeWithPrefix))
    case NitroTitles(episode, None, Some(series), None, Some(position)) => Titles(series, Some(s"$position. $episode"))
    case NitroTitles(episode, None, Some(series), None, None) => Titles(series, Some(episode))
    case NitroTitles(episode, None, None, Some(subSeries), _) => Titles(subSeries, Some(episode))
    case NitroTitles(episode, None, None, None, None) => Titles(episode, None)
  }

  def extractTitles(nitroEpisode: NodeSeq) = {
    val episode = (nitroEpisode \ "title" headOption).getOrElse(nitroEpisode \ "presentation_title")
    val brand = (nitroEpisode \ "ancestors_titles" \ "brand" \\ "title").headOption.map(_.text)
    val seriesTitles = (nitroEpisode \ "ancestors_titles" \ "series" \\ "title").reverse.take(2).reverse
    val series = seriesTitles.headOption.map(_.text.trim)
    val subseries = seriesTitles.drop(1).headOption.map(_.text)
    val position = (nitroEpisode \ "episode_of" \ "@position").headOption.map(_.text.toInt)

    NitroTitles(episode text, brand, series, subseries, position)
  }

  private def parseEpisode(bodyXml: Elem): Episode = {
    def parseImage(xml: NodeSeq) = Image((xml \ "@template_url" text).replace("$recipe", "{recipe}"), "image")
    def parseSynopses(xml: NodeSeq): Synopses = Synopses(xml \ "short" text, xml \ "medium" text, xml \ "long" text)
    def parseReleaseDate(xml: Option[Node]) = xml.map { node =>
      val dateTime = DateTime.parse(node.text, DateTimeFormat.forPattern("yyy-MM-ddZ"))
      dateTime.toString(DateTimeFormat.forPattern("dd MMM yyyy"))
    }
    val title = extractTitles(bodyXml \\ "results" \ "episode")

    Episode(
      bodyXml \\ "results" \ "episode" \ "pid" text,
      parseSynopses(bodyXml \\ "synopses"),
      parseImage(bodyXml \\ "image"),
      bodyXml \\ "episode_of" \\ "@pid" text,
      parseReleaseDate(bodyXml \\ "release_date" headOption),
      title.title,
      title.subtitle
    )
  }
}

case class NitroTitles(episode: String, brand: Option[String], series: Option[String], subSeries: Option[String], position: Option[Int]) {
  def episodeWithPrefix = position match {
    case Some(pos) => s": $pos. $episode"
    case None => s": $episode"
  }
}
