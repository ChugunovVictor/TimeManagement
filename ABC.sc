import java.time.LocalDateTime

import models.{History, HistoryType}

def difference(prev: LocalDateTime, current: LocalDateTime): (Int, Int) = {
  if( prev.getMinute > current.getMinute ){
    (current.getHour - 1 - prev.getHour, current.getMinute - prev.getMinute + 60 )
  } else
    (current.getHour - prev.getHour, current.getMinute - prev.getMinute )
}

def sum( prev: (Int, Int), current: (Int, Int) ): (Int, Int) = {
  val t = (current._1 + prev._1, current._2 + prev._2)
  (t._1 + t._2 / 60, t._2 % 60)
}

def period(histories: Seq[History], previous: Option[LocalDateTime] = None, hours: Int = 0, minutes: Int = 0): (Int, Int) = histories match {
  case Nil => (hours, minutes)
  case head :: Nil => previous match {
    case Some(prev) => head.`type` match {
      case HistoryType.Login => (hours, minutes)
      case HistoryType.Logout => {
        val diff = difference(prev, head.date)
        sum( (hours, minutes), diff )
      }
    }
    case None => (hours, minutes)
  }
  case head :: tail => previous match {
    case Some(prev) => head.`type` match {
      case HistoryType.Login => (hours, minutes)
      case HistoryType.Logout => {
        val diff = difference(prev, head.date)
        val newHM = sum( (hours, minutes), diff )
        period(tail, None, newHM._1, newHM._2)
      }
    }
    case None => head.`type` match {
      case HistoryType.Login => period(tail, Some(head.date), hours, minutes)
      case HistoryType.Logout => (hours, minutes)
    }
  }
}

val a = Seq(
  History("", LocalDateTime.of(2021, 3, 17, 8, 45), HistoryType.Login),
  History("", LocalDateTime.of(2021, 3, 17, 10, 30), HistoryType.Logout),
  History("", LocalDateTime.of(2021, 3, 17, 16, 30), HistoryType.Login),
  History("", LocalDateTime.of(2021, 3, 17, 18, 48), HistoryType.Logout),
)

period(a)
