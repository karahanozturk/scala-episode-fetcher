package controllers

import javax.inject.Inject

import org.json4s.FieldSerializer._
import org.json4s._
import org.json4s.jackson.Serialization.write
import play.api.mvc._
import prefetcher.{Episode, EpisodeFetcher}

import scala.concurrent.ExecutionContext.Implicits.global

class EpisodeController @Inject()(service: EpisodeFetcher) extends Controller {
  implicit val formats = DefaultFormats + JsonUtils.episodeSerializer

  def episodes(pid: String) = Action.async {
    service.episodes(pid) map {
      case None => NotFound
      case episode => Ok(write(episode))
    }
  }
}

object JsonUtils {
  val episodeSerializer = FieldSerializer[Episode](
    renameTo("parentId", "parent_id") orElse renameTo("releaseDate", "release_date"),
    renameFrom("parent_id", "parentId") orElse renameFrom("release_date", "releaseDate")
  )
}