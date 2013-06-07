package controllers

import play.api._
import mvc._
import controllers.Authentication.Secured
import views.{TaskQueryRepository}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.{ActorRef, Props}
import model.{EventStore, TaskCommandHandler}

object Application extends Controller with Secured {

  val eventStore: ActorRef = Akka.system.actorOf(Props[EventStore], name = "eventStore")
  Akka.system.actorOf(Props(new TaskCommandHandler(eventStore)), name = "taskCommandHandler")

  val taskQueryRepository = new TaskQueryRepository
  Akka.system.actorOf(Props(new TaskQueryRepository.Updater(eventStore, taskQueryRepository)), "taskQueryUpdater") ! "init"

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