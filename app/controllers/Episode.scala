package controllers


case class Episode(pid: String, synopses: Synopses, image: Image, parentId: String, releaseDate: Option[String] = None)

case class Synopses(small: String, medium: String, long: String)

case class Image(standard: String, `type`: String = "image")
