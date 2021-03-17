import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContext}
import javax.inject._
import models.{HistoryQueries, UserQueries, UserTable}
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[ApplicationStart]).asEagerSingleton()
  }

}

@Singleton
class ApplicationStart @Inject()(implicit ec: ExecutionContext){
  Await.result(UserQueries.setup(), Duration.Inf);
  Await.result(HistoryQueries.setup(), Duration.Inf);
}
