package services

import akka.actor.{Actor, Props}

object ScheduleService {
  def props = Props[ScheduleService]

  case class SayHello(name: String)
}

class ScheduleService extends Actor {
  import ScheduleService._

  override def receive: Receive = {
    case SayHello(name: String) => {
      println("hello, " + name)
    }
  }
}
