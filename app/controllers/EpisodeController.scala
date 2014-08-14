package controllers

import javax.inject.Inject

import org.json4s._
import org.json4s.FieldSerializer._
import org.json4s.jackson.Serialization.write
import play.api.mvc._
import service.EpisodeService
import scala.concurrent.ExecutionContext.Implicits.global

class EpisodeController @Inject()(service: EpisodeService) extends Controller {
  implicit val formats = DefaultFormats + JsonUtils.episodeSerializer
  
  def episodes(pid: String) = Action.async {
    service.episodes(pid) map { episode => Ok(write(episode)) }
  }
}

object JsonUtils {
  val episodeSerializer = FieldSerializer[Episode](
    renameTo("parentId", "parent_id"),
    renameFrom("parent_id", "parentId")
  )
}