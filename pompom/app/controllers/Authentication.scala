package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.nonEmptyText

object Authentication extends Controller {

  def login = Action {
    Ok(views.html.login())
  }

  val loginForm = Form(
    "email" -> nonEmptyText
  )

  def doLogin() = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        errors => BadRequest(views.html.login()),
        email => Redirect(routes.Application.index()).withSession(session + ("email" -> email)
        // TODO Direct user to where he was going when he came from
        )
      )
  }

  def doLogout() = Action {
    Ok(views.html.login()).withNewSession
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
    private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.login)

    // --

    /**
     * Action for authenticated users.
     */
    def AsCurrentUser(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
      user =>
        Action(request => f(user)(request))
    }

  }
}
