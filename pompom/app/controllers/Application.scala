package controllers

import play.api._
import mvc._
import controllers.Authentication.Secured

object Application extends Controller with Secured {

  def index = AsCurrentUser( user => Action { request =>
    if (request.host.startsWith("api"))
      Ok(views.html.apiIndex(user))
    else if (request.host.startsWith("www"))
      Ok(views.html.appIndex(user))
    else
      NotFound("Nothing to serve: " + request.host)
  })

}