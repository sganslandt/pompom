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

  Akka.system.actorOf(Props[EventStore], name = "eventStore")
  Akka.system.actorOf(Props[TaskCommandHandler], name = "taskCommandHandler")
  println("Starting updater and sending init")

  val taskQueryRepository = new TaskQueryRepository
  Akka.system.actorOf(Props(new TaskQueryRepository.Updater(taskQueryRepository))) ! "init"

  def index = AsAuthenticatedUser(userId => {
    request =>
      Ok(views.html.index(
        userId,
        taskQueryRepository.listForUser(userId)
      ))
  })

  def currentAuthenticatedUser = AsAuthenticatedUser {
    userId => {
      request =>
        Ok(views.html.auth.currentUser(userId))
    }
  }

}