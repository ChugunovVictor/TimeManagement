package models

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.TimeZone

import akka.Done
import models.HistoryType.HistoryType
import models.UserType.UserType
import play.api.libs.json._
import services.TemplateBuilder
import slick.jdbc.GetResult
import slick.jdbc.meta.MTable
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object HistoryType extends Enumeration {
  type HistoryType = Value
  val Login = Value("Login")
  val Logout = Value("Logout")

  implicit val readsHistory = Reads.enumNameReads(HistoryType)
  implicit val writesHistory = Writes.enumNameWrites
}

case class History(
                    userId: String,
                    date: LocalDateTime,
                    `type`: HistoryType,
                    id: Int = 0
                  )

object History {
  implicit val historyFormat = Json.format[History]

  implicit val getHistoryResult = GetResult(r => History(
    r.nextString, LocalDateTime.ofInstant(Instant.ofEpochMilli(r.nextTimestamp.getTime),
      TimeZone.getDefault().toZoneId()), HistoryType.withName(r.nextString), r.nextInt
  ))
}

class HistoryTable(tag: Tag) extends Table[History](tag, "history") {
  implicit val HistoryTypeMapper = MappedColumnType.base[HistoryType, String](
    e => e.toString,
    s => HistoryType.withName(s)
  )

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[String]("userId")

  def `type` = column[HistoryType]("type")

  def date = column[LocalDateTime]("historyDate")

  def * = (userId, date, `type`, id) <> ((History.apply _).tupled, History.unapply)

  def history_fk = foreignKey("userId", userId, App.users)(_.id)
}

object HistoryQueries {
  def setup()(implicit ec: ExecutionContext): Future[Any] = {
    App.db.run(
      for {
        tables <- MTable.getTables
        actions <- tables.find(p => p.name.name.equals("history")) match {
          case Some(value) => DBIO.successful(Done)
          case None => (App.histories.schema).create
        }
      } yield actions
    )
  }

  def insert(history: History)(implicit ec: ExecutionContext): Future[Int] = {
    import java.time.format.DateTimeFormatter
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val sql = sqlu"insert into history (userId, historyDate, type) values('#${
      history.userId
    }', DateTime('#${history.date.format(formatter)}'), '#${history.`type`}')"

    App.db.run(
      sql
    ).map(_ => history.id)
  }

  def list(from: LocalDateTime, to: LocalDateTime)(implicit ec: ExecutionContext): Future[Map[User, Seq[History]]] = {
    case class UserHistory(userId: String, firstName: String, lastName: String, userType: UserType,
                           email: Option[String], password: Option[Int], isActive: Boolean,
                           date: Option[Timestamp], historyType: Option[HistoryType], historyId: Option[Int])

    object UserHistory {
      implicit val getUserHistoryResult = GetResult(r => UserHistory(
        r.nextString, r.nextString, r.nextString, UserType.withName(r.nextString), r.nextStringOption, r.nextIntOption, r.nextBoolean,
        r.nextTimestampOption, r.nextStringOption.map(HistoryType.withName), r.nextIntOption
      ))

      def to(uh: UserHistory): (User, Option[History]) = {
        val user = User(
          firstName = uh.firstName, lastName = uh.lastName,
          id = uh.userId, `type` = uh.userType, isActive = uh.isActive,
          email = uh.email, password = uh.password,
        )

        val historyOpt = uh.historyId.map(_ =>
          History(
            userId = uh.userId,
            date = uh.date.get.toLocalDateTime,
            `type` = uh.historyType.get,
            id = uh.historyId.get,
          )
        )
        (user, historyOpt)
      }
    }

    val dateFromFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(from)
    val dateToFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(to)

    val query = sql"""select u.id, u.firstName, u.lastName, u.type, u.email, u.password, u.isActive,
          strftime('%Y-%m-%d %H:%M:%f', h.historyDate), h.type, h.id
      from users u left join history h on (u.id = h.userId and
          Date(h.historyDate, 'localtime') >= Date('#$dateFromFormatted') and Date(h.historyDate, 'localtime') <= Date('#$dateToFormatted'))
     """.as[UserHistory]

    App.db.run(query)
      .map(_.map(r => UserHistory.to(r)))
      .map(_.groupBy(_._1))
      .map(_.filter(r => r._1.`type` == UserType.Mechanic))
      .map(_.map(r => (r._1, r._2.map(_._2).filterNot(_.isEmpty).map(_.get))))
      .map(_.filterNot(r => !r._1.isActive && r._2.isEmpty))
  }

  def userList(date: LocalDateTime, userId: String)(implicit ec: ExecutionContext): Future[Seq[History]] = {
    val dateFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)

    val query = sql"""select h.userId, strftime('%Y-%m-%d %H:%M:%f', h.historyDate), h.type, h.id
      from history h where h.userId = '#$userId' and Date(h.historyDate) = Date('#$dateFormatted')
     """.as[History]

    App.db.run(query)
  }


  // For android
  case class Ping(
                   userId: String,
                   userName: String,
                   isLogged: Boolean,
                   workingTime: String
                 )

  object Ping {
    implicit val pingFormat = Json.format[Ping]
  }

  def ping()(implicit ec: ExecutionContext): Future[Seq[Ping]] = {
    implicit class OrderedLocalDateTime(time: LocalDateTime) extends Ordered[LocalDateTime] {
      override def compare(that: LocalDateTime): Int = time.compareTo(that)
    }

    for {
      all <- list(LocalDateTime.now, LocalDateTime.now)
    } yield
      all.map(r => {
        val sorted = r._2.sortBy(_.date)
        val (isLogged, (totalHours, totalMinutes), previous) = sorted.foldLeft[(Boolean, (Int, Int), Option[(Int, Int)])]((false, (0, 0), None))(
          (acc, c) => c.`type` match {
            case HistoryType.Logout => acc._3 match {
              case Some(value) => (false, TemplateBuilder.sum(
                acc._2, TemplateBuilder.differenceT(value, (c.date.getHour, c.date.getMinute))
              ), acc._3)
              case None => (false, acc._2, None)
            }
            case HistoryType.Login => (true, acc._2, Some(c.date.getHour, c.date.getMinute))
          }
        )

        val (resultHours, resultMinutes) = sorted.lastOption match {
          case None => (totalHours, totalMinutes)
          case Some(value) => value.`type` match {
            case HistoryType.Logout => (totalHours, totalMinutes)
            case HistoryType.Login => TemplateBuilder.sum(
              (totalHours, totalMinutes), TemplateBuilder.differenceT((value.date.getHour, value.date.getMinute), (LocalDateTime.now().getHour, LocalDateTime.now().getMinute))
            )
          }
        }

        Ping(
          userId = r._1.id,
          userName = s"${r._1.firstName} ${r._1.lastName}",
          isLogged = isLogged,
          workingTime = TemplateBuilder.timeToString((resultHours, resultMinutes)))
      }).toSeq
  }

  def logInOut(userId: String, password: Int)(implicit ec: ExecutionContext): Future[String] = {
    val lastHistoryTypeQuery = sql"select h.type from history h where h.userId='#$userId' and Date(h.historyDate, 'localtime') >= Date('now', 'localtime') and Date(h.historyDate, 'localtime') <= Date('now', 'localtime') order by h.historyDate desc limit 1"

    def updateStatus(lastStatus: Option[String]) ={
      lastStatus match {
        case Some(value) => HistoryType.withName(value) match {
          case HistoryType.Login => App.db.run(sqlu"insert into history (userId, historyDate, type) values('#$userId', DateTime('now', 'localtime'), '#${HistoryType.Logout}')").map(_ => "Success")
          case HistoryType.Logout => App.db.run(sqlu"insert into history (userId, historyDate, type) values('#$userId', DateTime('now', 'localtime'), '#${HistoryType.Login}')").map(_ => "Success")
        }
        case _ =>
          App.db.run(sqlu"insert into history (userId, historyDate, type) values('#$userId', DateTime('now', 'localtime'), '#${HistoryType.Login}')").map(_ => "Success")
      }
    }

    for {
      user <- UserQueries.get(userId)
      lastHistoryType <- App.db.run(lastHistoryTypeQuery.as[String].headOption)
      result <- user match {
        case Some(u) => if (u.password.getOrElse(-1) == password) {
          updateStatus(lastHistoryType)
        } else Future.successful("Incorrect password")
        case None => Future.successful("User not found")
      }
    } yield result
  }

}
