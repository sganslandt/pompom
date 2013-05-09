package controllers

import play.api._
import mvc._
import controllers.Authentication.Secured
import views.{TaskQueryRepository}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import model.{EventStore, TaskCommandHandler}

object Application extends Controller with Secured {

  val eventStore = Akka.system.actorOf(Props[EventStore], name = "eventStore")
  Akka.system.actorOf(Props(new TaskCommandHandler(eventStore)), name = "taskCommandHandler")
  println("Starting updater and sending init")
  Akka.system.actorOf(Props[TaskQueryRepository.Updater]) ! "init"

  def index = AsAuthenticatedUser(userId => {
    request =>
      Ok(views.html.index(
        userId,
        TaskQueryRepository.listForUser(userId)
      ))
  })

  def currentAuthenticatedUser = AsAuthenticatedUser {
    userId => {
      request =>
        Ok(views.html.auth.currentUser(userId))
    }
  }

}