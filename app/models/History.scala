package models

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.Done
import models.HistoryType.HistoryType
import models.UserType.UserType
import play.api.libs.json._
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

object HistoryQueries{


  def setup()(implicit ec : ExecutionContext): Future[Any] = {
    App.db.run(
      for{
        tables <- MTable.getTables
        actions <- tables.find(p => p.name.name.equals("history")) match {
          case Some(value) => DBIO.successful(Done)
          case None => (App.histories.schema).create
        }
      } yield actions
    )
  }

  def insert(history: History)(implicit ec : ExecutionContext): Future[Int] = {
    import java.time.format.DateTimeFormatter
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val sql = sqlu"insert into history (userId, historyDate, type) values('#${history.userId
        }', DateTime('#${history.date.format(formatter)}'), '#${history.`type`}')"

    App.db.run(
      sql
    ).map(_ => history.id)
  }

  def list(from: LocalDateTime, to: LocalDateTime)(implicit ec : ExecutionContext): Future[Map[User, Seq[History]]] = {
    case class UserHistory(userId: String, firstName: String, lastName: String, userType: UserType,
                           email: Option[String], password: Option[Int], isActive: Boolean,
                           date: Option[Timestamp], historyType: Option[HistoryType], historyId: Option[Int])

    object UserHistory{
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
        ( user, historyOpt )
      }
    }

    val dateFromFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(from)
    val dateToFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(to)

    val query = sql"""select u.id, u.firstName, u.lastName, u.type, u.email, u.password, u.isActive,
          strftime('%Y-%m-%d %H:%M:%f', h.historyDate), h.type, h.id
      from users u left join history h on (u.id = h.userId and
          Date(h.historyDate) >= Date('#$dateFromFormatted') and Date(h.historyDate) <= Date('#$dateToFormatted'))
     """.as[UserHistory]

    App.db.run(query)
      .map(_.map(r => UserHistory.to(r)))
      .map(_.groupBy(_._1))
      .map(_.filter(r => r._1.`type` == UserType.Mechanic))
      .map(_.map(r => (r._1, r._2.map(_._2).filterNot(_.isEmpty).map(_.get))))
      .map(_.filterNot(r => !r._1.isActive && r._2.isEmpty ))
  }


}
