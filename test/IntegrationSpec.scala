import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Episode Fetcher" should {

    "given an episode exists in Nitro" in {

      // mock server response

      "returns the iBL episode when Nitro" in new WithBrowser {
        browser.goTo(s"http://localhost:$port/episodes?pid=p00lfrb3")

        browser.pageSource must contain(
          """
            |{
            |  "pid": "p00lfrb3"
            |}
          """.stripMargin)
      }
    }

//    "given an episode doesn't exist in Nitro" in {
//      "returns a not found response" in {
//
//      }
//    }
  }
}
