package services

import java.time.{DayOfWeek, LocalDateTime}

import akka.Done
import akka.actor.{Actor, Props}
import javax.inject.Inject
import models.{History, HistoryQueries, UserQueries, UserType}
import play.api.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


class CleanService @Inject()(ec: ExecutionContext) extends Actor {
  val logger: Logger = Logger(this.getClass())
  implicit val ctx = ec


  import CleanService._

  override def receive: Receive = {
    case CleanSend(value) => {
      logger.debug("CleanSend check: " + LocalDateTime.now().toString);
      if (LocalDateTime.now().getHour() == 19 && LocalDateTime.now().getDayOfWeek == DayOfWeek.FRIDAY){
        Await.result(HistoryQueries.logInOutAll(LocalDateTime.now()), Duration.Inf)
      }
    }
  }
}

object CleanService {
  def props = Props[CleanService]
  val NAME = "CleanService"

  case class CleanSend(str:String)
  //case class Tick()
}

