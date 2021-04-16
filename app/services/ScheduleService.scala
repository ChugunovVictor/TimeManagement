package services

package services

import java.time.{Instant, LocalDateTime, ZoneId}

import akka.actor.{Actor, Props}
import javax.inject.Inject
import models.{UserQueries, UserType}
import play.api.Logger

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

object ScheduleService {
  def props = Props[ScheduleService]

  case class Send(str:String)
  //case class Tick()
}

class ScheduleService @Inject()(ec: ExecutionContext, ms: MailerService) extends Actor {
  val logger: Logger = Logger(this.getClass())
  implicit val ctx = ec
  import ScheduleService._

  override def receive: Receive = {
    /*case Tick() => {
      logger.info("Tick");
    }*/
    case Send(value) => {
      if (LocalDateTime.now().getHour() == 19) {
        val users = Await.result(UserQueries.list(), Duration.Inf).filter(_.isActive).filter(_.`type` == UserType.Manager)
        ms.sendEmail( users.map(_.email.get), TemplateBuilder.make(LocalDateTime.now()))
      }
    }
  }
}
