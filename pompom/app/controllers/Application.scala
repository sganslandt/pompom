package controllers

import play.api._
import mvc._
import controllers.Authentication.Secured

object Application extends Controller with Secured {

  def index = AsAuthenticatedUser(userId => Action {
    request =>
      if (request.host.startsWith("api"))
        Ok(views.html.apiIndex())
      else if (request.host.startsWith("www"))
        Ok(views.html.appIndex(userId))
      else
        NotFound("Nothing to serve: " + request.host)
  })

  def currentAuthenticatedUser = AsAuthenticatedUser {
    userId =>
      Action {
        Ok(views.html.auth.currentUser(userId))
      }
  }

}