package services

import java.time.{LocalDateTime, Period, ZoneId}

import models.{History, HistoryQueries, HistoryType, User}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object TemplateBuilder {
  def step(n: Int, date: LocalDateTime): LocalDateTime = {
    if (n == 0 || date.getDayOfWeek.name.equalsIgnoreCase("Saturday")) date
    else step(n - 1, date.minusDays(1))
  }

  def title(start: LocalDateTime, end: LocalDateTime): String = {
    (start, end) match {
      case (s, e) if s.getYear != e.getYear => s"Time Sheet for the ${s.getDayOfMonth}th of ${s.getMonth.name.toLowerCase.capitalize} ${
        s
          .getYear
      } to ${e.getDayOfMonth}th of ${e.getMonth.name.toLowerCase.capitalize} ${e.getYear}"
      case (s, e) if (s.getYear == e.getYear && s.getMonthValue != e.getMonthValue) => s"Time Sheet for the ${
        s
          .getDayOfMonth
      }th of ${
        s.getMonth.name.toLowerCase.capitalize
      } to ${e.getDayOfMonth}th of ${e.getMonth.name.capitalize} ${e.getYear}"
      case _ => s"Time Sheet for the ${start.getDayOfMonth}th to ${end.getDayOfMonth}th of ${
        start
          .getMonth.name.toLowerCase.capitalize
      } ${start.getYear}"
    }
  }

  def dateToLong(date: LocalDateTime): Long = {
    date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
  }

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

  def row(user: User, histories: Seq[History]): (String, (Int, Int)) = {
    var row = scala.io.Source.fromFile(getClass.getResource("/template/row.html").getFile()).getLines.mkString

    row = row.replaceAll("::firstname", user.firstName)
    row = row.replaceAll("::lastname", user.lastName)
    val pre = histories.sortBy(_.date.getDayOfMonth).groupBy(_.date.getDayOfWeek.name.toLowerCase).map { case (i,
    dates) => (i, dates.sortBy(r => dateToLong(r.date)))
    }

    val rowTotal = pre.foldLeft((0,0))((acc, day) => {
        val loginLogout = day._2.partition(_.`type` == HistoryType.Login)
        row = row.replaceAll(s"::${day._1}_start", s"${loginLogout._1.headOption.map(r => s"${r.date.getHour}:${r.date.getMinute}").getOrElse("")}")
        row = row.replaceAll(s"::${day._1}_end", s"${loginLogout._2.lastOption.map(r => s"${r.date.getHour}:${r.date.getMinute}").getOrElse("")}")

        val dailyTotal = period(day._2.toList)
        row = row.replaceAll(s"::${day._1}_total", s"${dailyTotal._1}:${dailyTotal._2}")
        sum( acc, dailyTotal)
      })

    row = row.replaceAll(s"::row_total", s"${rowTotal._1}:${rowTotal._2}")

    (row, rowTotal)
  }

  def make(date: LocalDateTime)(implicit ec: ExecutionContext): String = {
    var table = scala.io.Source.fromFile(getClass.getResource("/template/table.html").getFile()).getLines.mkString

    val start = step(7, date)
    val end = start.plusDays(6)

    table = table.replaceAll("::title", title(start, end))

    val userInfo = Await.result(HistoryQueries.list(start, end), Duration.Inf)
    val ( rows, total ) = userInfo.foldLeft(("", (0,0))) ( (acc, c) => {
      val current = row(c._1, c._2)
      (acc._1 + current._1, sum( acc._2, current._2))
    })

    table = table.replaceAll("::rows", rows)
    table = table.replaceAll("::total", s"${total._1}:${total._2}")

    table = table.replaceAll("::[A-Za-z_]+", "")

    table
  }
}
