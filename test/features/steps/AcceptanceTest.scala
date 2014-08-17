package features.steps

import cucumber.api.junit.Cucumber
import cucumber.api.junit.Cucumber.Options
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@cucumber.api.CucumberOptions(
  features = Array("test/features"),
  glue = Array("features.steps"),
  format = Array("pretty", "html:target/cucumber-report"),
  tags = Array("~@wip")
)
class AcceptanceTest {
}
