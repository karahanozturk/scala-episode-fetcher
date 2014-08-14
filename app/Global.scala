
import com.google.inject.{AbstractModule, Guice}
import play.api.GlobalSettings

object Global extends GlobalSettings {

  val injector = Guice.createInjector(new AbstractModule {
    protected def configure() = {}
  })

  override def getControllerInstance[A](controllerClass: Class[A]): A = injector.getInstance(controllerClass)
}