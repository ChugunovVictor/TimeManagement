import akka.actor.{ActorSystem, ExtendedActorSystem, Props}
import com.google.inject.AbstractModule
import com.typesafe.akka.`extension`.quartz.QuartzSchedulerExtension

import scala.concurrent.{Await, ExecutionContext}
import javax.inject._
import models.{HistoryQueries, UserQueries}
import play.api.inject.ApplicationLifecycle
import play.inject.Injector
import services.{GuiceActorProducer, MailerService}
import services.services.ScheduleService

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
class ApplicationStart @Inject()(implicit ec: ExecutionContext,  lifecycle: ApplicationLifecycle,
                                 system: ActorSystem,
                                 injector: Injector){
  Await.result(UserQueries.setup(), Duration.Inf);
  Await.result(HistoryQueries.setup(), Duration.Inf);

  // Start scheduling
  val scheduler = QuartzSchedulerExtension(system)
  val receiver = system.actorOf(Props.create(classOf[GuiceActorProducer], injector, classOf[ScheduleService]))
  scheduler.schedule("sendEmail", receiver, ScheduleService.Send("now"), None)
}

