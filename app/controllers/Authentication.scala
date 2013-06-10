package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.ActorRef
import model.api.LoginUserCommand

object Authentication extends Controller {

  val taskCommandHandler: ActorRef = Akka.system.actorFor("/user/taskCommandHandler")

  def login = Action {
    Ok(views.html.auth.login())
  }

  val loginForm = Form(
    "email" -> nonEmptyText
  )

  def doLogin() = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        errors => BadRequest(views.html.auth.login()),
        email => {
          taskCommandHandler ! LoginUserCommand(email)
          Redirect(routes.Application.index(section = "")).withSession(session + ("email" -> email))
        }
      )
  }

  def doLogout() = Action {
    Results.Redirect(routes.Authentication.login()).withNewSession
  }

  /**
   * Provide security features
   */
  trait Secured {

    /**
     * Retrieve the connected user email.
     */
    private def username(request: RequestHeader) = request.session.get("email")

    /**
     * Redirect to login if the user in not authorized.
     */
    private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.login())

    // --

    /**
     * Action for authenticated users.
     */
    def AsAuthenticatedUser(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
      user =>
        Action(request => f(user)(request))
    }

  }
}
