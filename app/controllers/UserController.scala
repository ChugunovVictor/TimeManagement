package controllers

import java.time.{Instant, LocalDateTime, ZoneId}

import javax.inject._
import models.{User, UserQueries}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.json._
import services.{MailerService, TemplateBuilder}

import scala.concurrent.Future
// you need this import to have combinators
import play.api.libs.functional.syntax._


import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(cc: ControllerComponents, ms: MailerService) extends AbstractController(cc) {

  import models.User._

  def report(email: String, date: Long): Action[AnyContent] = Action { implicit request =>
      val normalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())
      val r = TemplateBuilder.make(normalDate)
      ms.sendEmail(Seq(s"TimeManagement TO <$email>"), r);
      Ok(Json.toJson("Operation complete"))
  }

  def list(): Action[AnyContent] = Action.async { implicit request =>
    UserQueries.list().map { list =>
      Ok(Json.toJson(list))
    }
  }

  def save = Action.async { implicit request =>
    request.body.asJson.map { json =>
      json.validate[User].map {
        user => {
          val userId = UserQueries.insertOrUpdate(user)
          userId.map(c => Ok(Json.toJson(c)))
        }
      }.recoverTotal { error =>
        Future.successful(BadRequest(Json.toJson(error.toString)))
      }
    }.getOrElse(
      Future.successful(BadRequest(Json.toJson("Empty body")))
    )
  }

  def delete(id: String) = Action.async { implicit request =>
    UserQueries.delete(id).map { _ =>
      Ok(Json.toJson("Deleted"))
    }
  }
}
