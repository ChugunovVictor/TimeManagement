package models

import akka.Done
import play.api.libs.json._
import models.UserType.UserType
import slick.jdbc.meta.MTable
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.CanBeQueryCondition

import scala.concurrent.{ExecutionContext, Future}

object UserType extends Enumeration {
  type UserType = Value
  val Manager = Value("Manager")
  val Mechanic = Value("Mechanic")

  implicit val readsUserType = Reads.enumNameReads(UserType)
  implicit val writesUserType = Writes.enumNameWrites
}

case class User(
                 firstName: String,
                 lastName: String,
                 `type`: UserType,
                 email: Option[String],
                 password: Option[Int],
                 isActive: Boolean,
                 id: String
               )

object User {
  implicit val userFormat = Json.format[User]
}

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  implicit val userTypeMapper = MappedColumnType.base[UserType, String](
    e => e.toString,
    s => UserType.withName(s)
  )

  def id = column[String]("id", O.PrimaryKey)
  def firstName = column[String]("firstName")
  def lastName = column[String]("lastName")
  def `type` = column[UserType]("type")
  def email = column[Option[String]]("email")
  def password = column[Option[Int]]("password")
  def isActive = column[Boolean]("isActive")

  def * = (firstName, lastName, `type`, email, password, isActive, id) <> ((User.apply _).tupled, User.unapply)

  implicit val userTypeColumnCanBeQueryCondition : CanBeQueryCondition[Rep[UserType]] =
    new CanBeQueryCondition[Rep[UserType]] {
      def apply(value: Rep[UserType]) = value
    }
}

object UserQueries{

  def setup()(implicit ec : ExecutionContext): Future[Any] = {
    App.db.run(
      for{
        tables <- MTable.getTables
        actions <- tables.find(p => p.name.name.equals("users")) match {
          case Some(value) => DBIO.successful(Done)
          case None => (App.users.schema).create
        }
      } yield actions
    )
  }

  def insertOrUpdate(user: User)(implicit ec : ExecutionContext): Future[String] = {
    val query = sqlu"""
        INSERT INTO users(id,firstName,lastName,type,email,password,isActive) VALUES(
          '#${user.id}', '#${user.firstName}', '#${user.lastName}', '#${user.`type`}', #${user.email.map("\"" + _ + "\"").getOrElse("NULL")},
          #${user.password.getOrElse("NULL")}, #${if (user.isActive) 1 else 0} ) ON CONFLICT(id) DO UPDATE SET firstName = excluded.firstName, lastName = excluded.lastName,
             'type' = excluded.type, email = excluded.email, password = excluded.password, isActive = excluded.isActive;
          """
    App.db.run(query).map(_ => user.id)
  }

  def list()(implicit ec : ExecutionContext): Future[Seq[User]] = {
    App.db.run(App.users.result)
  }

  def delete(id: String)(implicit ec : ExecutionContext): Future[Done] = {
    App.db.run(App.users.filter(_.id === id).delete).map(_ => Done)
  }
}
