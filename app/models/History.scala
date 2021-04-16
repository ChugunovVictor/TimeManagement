package models

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
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

case class AdminLoginLogout(
                             userId: String,
                             date: Long,
                             login: String,
                             logout: String
                           )

object AdminLoginLogout {
  implicit val adminLoginLogoutFormat = Json.format[AdminLoginLogout]
}

case class History(
                    userId: String,
                    date: LocalDateTime,
                    `type`: HistoryType,
                    id: Int = 0
                  )

object History {
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  implicit val historyFormat = Json.format[History]

  implicit val getHistoryResult = GetResult(r => History(
    r.nextString, LocalDateTime.parse(r.nextString, formatter), HistoryType.withName(r.nextString), r.nextInt
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

  def list(from: Option[LocalDateTime], to: LocalDateTime)(implicit ec: ExecutionContext): Future[Map[User, Seq[History]]] = {
    case class UserHistory(userId: String, firstName: String, lastName: String, userType: UserType,
                           email: Option[String], password: Option[Int], isActive: Boolean,
                           date: Option[LocalDateTime], historyType: Option[HistoryType], historyId: Option[Int])

    object UserHistory {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

      implicit val getUserHistoryResult = GetResult(r => UserHistory(
        r.nextString, r.nextString, r.nextString, UserType.withName(r.nextString), r.nextStringOption, r.nextIntOption, r.nextBoolean,
        r.nextStringOption.map(p => LocalDateTime.parse(p, formatter)), r.nextStringOption.map(HistoryType.withName), r.nextIntOption
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
            date = uh.date.get,
            `type` = uh.historyType.get,
            id = uh.historyId.get,
          )
        )
        (user, historyOpt)
      }
    }

    val query = from match {
      case Some(value) => {
        val dateFromFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(value)
        val dateToFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(to)

        sql"""select u.id, u.firstName, u.lastName, u.type, u.email, u.password, u.isActive,
          h.historyDate, h.type, h.id
      from users u left join history h on (u.id = h.userId and
          Date(h.historyDate) >= Date('#$dateFromFormatted') and DateTime(h.historyDate) <= DateTime('#$dateToFormatted'))
     """.as[UserHistory]
      }
      case None => {
        sql"""select u.id, u.firstName, u.lastName, u.type, u.email, u.password, u.isActive,
          h.historyDate, h.type, h.id
      from users u left join history h on (u.id = h.userId and
          date(h.historyDate) >= date('now', 'localtime') and
          datetime(h.historyDate) <= datetime('now', 'localtime'))
     """.as[UserHistory]
      }
    }

    App.db.run(query)
      .map(_.map(r => UserHistory.to(r)))
      .map(_.groupBy(_._1))
      .map(_.filter(r => r._1.`type` == UserType.Mechanic))
      .map(_.map(r => (r._1, r._2.map(_._2).filterNot(_.isEmpty).map(_.get))))
      .map(_.filterNot(r => !r._1.isActive && r._2.isEmpty))
  }

  def userList(date: LocalDateTime, userId: String)(implicit ec: ExecutionContext): Future[Seq[History]] = {
    val dateFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)

    val query = sql"""select h.userId, h.historyDate, h.type, h.id
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
      all <- list(Some(LocalDateTime.now), LocalDateTime.now)
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

  def updateStatus(userId: String, lastStatus: Option[String])(implicit ec: ExecutionContext): Future[String] = {
    lastStatus match {
      case Some(value) => HistoryType.withName(value) match {
        case HistoryType.Login => App.db.run(sqlu"insert into history (userId, historyDate, type) values('#$userId', DateTime('now', 'localtime'), '#${HistoryType.Logout}')").map(_ => "Success")
        case HistoryType.Logout => App.db.run(sqlu"insert into history (userId, historyDate, type) values('#$userId', DateTime('now', 'localtime'), '#${HistoryType.Login}')").map(_ => "Success")
      }
      case _ =>
        App.db.run(sqlu"insert into history (userId, historyDate, type) values('#$userId', DateTime('now', 'localtime'), '#${HistoryType.Login}')").map(_ => "Success")
    }
  }

  def logInOutAdmin(userId: String)(implicit ec: ExecutionContext): Future[String] = {
    val lastHistoryTypeQuery =
      sql"""select h.type from history h where h.userId='#$userId' and Date(h.historyDate)
            >= Date('now') order by h.historyDate desc limit 1"""

    for {
      lastHistoryType <- App.db.run(lastHistoryTypeQuery.as[String].headOption)
      result <- updateStatus(userId, lastHistoryType)
    } yield result
  }

  def logInOutAll(date: LocalDateTime)(implicit ec: ExecutionContext): Future[String] = {
    implicit class OrderedLocalDateTime(time: LocalDateTime) extends Ordered[LocalDateTime] {
      override def compare(that: LocalDateTime): Int = time.compareTo(that)
    }
    val correctedDate = date.withHour(19).withMinute(0)
    val dateToFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(correctedDate)
    (for{
      users <- HistoryQueries.list(Some(date), correctedDate.withMinute(1))
      _ <- Future.sequence(
        users.filter { case (user, histories) =>
          !histories.isEmpty && histories.sortBy(_.date).last.`type` == HistoryType.Login
        }.map(r =>
          App.db.run(sqlu"insert into history (userId, historyDate, type) values('#${r._1.id}', DateTime('#$dateToFormatted'), '#${HistoryType.Logout}')").map(_ => "Success")
        )
      )
    } yield Done).map(_ => "Done")
  }

  def logInOutAdminForParticularDate(aLL: AdminLoginLogout)(implicit ec: ExecutionContext): Future[String] = {
    val normalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(aLL.date), ZoneId.systemDefault())

    val loginHistoryAction = History(
      userId = aLL.userId,
      date = {
        normalDate.withHour(aLL.login.split(":")(0).toInt).withMinute(aLL.login.split(":")(1).toInt)
      },
      `type` = HistoryType.Login
    )

    val logoutHistoryAction = History(
      userId = aLL.userId,
      date = {
        normalDate.withHour(aLL.logout.split(":")(0).toInt).withMinute(aLL.logout.split(":")(1).toInt)
      },
      `type` = HistoryType.Logout
    )

    (for {
      _ <- HistoryQueries.insert(loginHistoryAction)
      _ <- HistoryQueries.insert(logoutHistoryAction)
    } yield ()).map(_ => "Success")
  }

  def logInOut(userId: String, password: Int)(implicit ec: ExecutionContext): Future[String] = {
    val lastHistoryTypeQuery =
      sql"""select h.type from history h where h.userId='#$userId' and Date(h.historyDate)
            >= Date('now') order by h.historyDate desc limit 1"""

    for {
      user <- UserQueries.get(userId)
      lastHistoryType <- App.db.run(lastHistoryTypeQuery.as[String].headOption)
      result <- user match {
        case Some(u) => if (u.password.getOrElse(-1) == password) {
          updateStatus(userId, lastHistoryType)
        } else Future.successful("Incorrect password")
        case None => Future.successful("User not found")
      }
    } yield result
  }

}
