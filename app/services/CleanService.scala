package services

import java.time.LocalDateTime

import akka.Done
import akka.actor.{Actor, Props}
import javax.inject.Inject
import models.{History, HistoryQueries, UserQueries, UserType}
import play.api.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


class CleanService @Inject()(ec: ExecutionContext, ms: MailerService) extends Actor {
  val logger: Logger = Logger(this.getClass())
  implicit val ctx = ec


  import CleanService._

  override def receive: Receive = {
    /*case Tick() => {
      logger.info("Tick");
    }*/
    case CleanSend(value) => {
      if (LocalDateTime.now().getHour() == 19){
      val action = for{
        users <- HistoryQueries.ping
        _ <- Future.sequence(
          users.filter(_.isLogged).map(r=> HistoryQueries.logInOutAdmin(r.userId))
        )
      } yield Done
      Await.result(action, Duration.Inf)
    }}
  }
}

object CleanService {
  def props = Props[CleanService]

  case class CleanSend(str:String)
  //case class Tick()
}

