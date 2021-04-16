package services

import java.time.{DayOfWeek, LocalDateTime}

import akka.actor.Actor
import javax.inject.Inject
import models.{HistoryQueries, UserQueries, UserType}
import play.api.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}


class StartService @Inject()(ec: ExecutionContext, ms: MailerService) extends Actor {
  val logger: Logger = Logger(this.getClass())
  implicit val ctx = ec

  override def receive: Receive = {
    case message => {
      val time = LocalDateTime.now
      if (time.getHour() == 19 && time.getMinute() == 0){
        Await.result(HistoryQueries.logInOutAll(LocalDateTime.now()), Duration.Inf)
      }
      if (time.getHour() == 19 && time.getMinute() == 1 && LocalDateTime.now().getDayOfWeek == DayOfWeek.FRIDAY){
        val users = Await.result(UserQueries.list(), Duration.Inf).filter(_.isActive).filter(_.`type` == UserType.Manager)
        ms.sendEmail( users.map(_.email.get), TemplateBuilder.make(LocalDateTime.now()))
      }
    }
  }
}



