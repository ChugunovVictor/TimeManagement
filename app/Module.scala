import akka.actor.{ActorSystem, Props}
import com.google.inject.AbstractModule

import scala.concurrent.{Await, ExecutionContext}
import javax.inject._
import models.{HistoryQueries, UserQueries}
import play.api.inject.ApplicationLifecycle
import play.inject.Injector
import services.{MailerService, StartService}

import scala.concurrent.duration.{Duration, DurationInt}

class Module extends AbstractModule{

  override def configure() = {
    bind(classOf[ApplicationStart]).asEagerSingleton()
  }
}

@Singleton
class ApplicationStart @Inject()(implicit ec: ExecutionContext,  lifecycle: ApplicationLifecycle,
                                 system: ActorSystem,
                                 injector: Injector, ms: MailerService){
  Await.result(UserQueries.setup(), Duration.Inf);
  Await.result(HistoryQueries.setup(), Duration.Inf);

  val schedule = akka.actor.ActorSystem("system")
  val scheduleActor = schedule.actorOf(Props(classOf[StartService], ec, ms))
  schedule.scheduler.scheduleWithFixedDelay( Duration.Zero, 1.minute, scheduleActor, "now")
}

