package controllers

import java.time.{Instant, LocalDateTime, ZoneId}

import javax.inject._
import models.{History, HistoryQueries, User, UserQueries}
import play.api.libs.json.Json
import play.api.mvc._
import services.TemplateBuilder

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HistoryController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def list(date: Long): Action[AnyContent] = Action.async { implicit request =>
    val normalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())
    HistoryQueries.list( normalDate, normalDate ).map { list =>
      Ok(Json.toJson(list))
    }
  }

  def report(date: Long): Action[AnyContent] = Action { implicit request =>
    val normalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())
    val r = TemplateBuilder.make(normalDate)
    Ok(Json.toJson(r))
  }

  def save = Action.async { implicit request =>
    request.body.asJson.map { json =>
      json.validate[History].map {
        history => {
          val historyId = HistoryQueries.insert(history)
          historyId.map(c => Ok(Json.toJson(c)))
        }
      }.recoverTotal { error =>
        Future.successful(BadRequest(Json.toJson(error.toString)))
      }
    }.getOrElse(
      Future.successful(BadRequest(Json.toJson("Empty body")))
    )
  }
}
