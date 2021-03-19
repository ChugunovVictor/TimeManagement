import akka.actor.{ActorSystem, Props}
import com.google.inject.AbstractModule
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import javax.inject._
import models.{HistoryQueries, UserQueries}
import play.api.inject.ApplicationLifecycle
import play.inject.Injector
import services.ScheduleService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

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
class ApplicationStart @Inject()(implicit ec: ExecutionContext,  lifecycle: ApplicationLifecycle,
                                 system: ActorSystem,
                                 injector: Injector){
  Await.result(UserQueries.setup(), Duration.Inf);
  Await.result(HistoryQueries.setup(), Duration.Inf);

  val scheduler = QuartzSchedulerExtension.get(system)
  val actor = system.actorOf(Props(classOf[ScheduleService]))
  scheduler.schedule("Every30Seconds", actor, ScheduleService.SayHello("Peter"))
}
