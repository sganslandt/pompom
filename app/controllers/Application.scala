package controllers

import play.api._
import mvc._
import controllers.Authentication.Secured
import views.TaskQueryRepository

object Application extends Controller with Secured {

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