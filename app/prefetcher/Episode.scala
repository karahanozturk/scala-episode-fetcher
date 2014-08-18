package prefetcher

case class Episode(pid: String, synopses: Synopses, image: Image, parentId: String, releaseDate: Option[String] = None,
                    title: String, subtitle: Option[String] = None)

case class Image(standard: String, `type`: String = "image")
case class Synopses(small: String, medium: String, long: String)
case class Titles(title: String, subtitle: Option[String])