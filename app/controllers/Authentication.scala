package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.ActorRef
import model.api.{RegisterUserCommand, LoginUserCommand}
import scala.concurrent.ExecutionContext
import play.api.libs.openid.OpenID
import ExecutionContext.Implicits.global

object Authentication extends Controller {

  val taskCommandHandler: ActorRef = Akka.system.actorFor("/user/taskCommandHandler")

  def login = Action {
    Ok(views.html.auth.login())
  }

  def doLogout() = Action {
    Redirect(routes.Authentication.login()).withNewSession
  }

  def auth = Action {
    implicit request => {
      val attributes = List(
        "email" -> "http://schema.openid.net/contact/email",
        "firstname" -> "http://schema.openid.net/namePerson/first",
        "lastname" -> "http://schema.openid.net/namePerson/last"
      )

      AsyncResult(
        OpenID.redirectURL(
          "https://www.google.com/accounts/o8/id",
          routes.Authentication.verify.absoluteURL(),
          attributes
        ).map(url =>
          Redirect(url)
        )
      )
    }
  }

  def verify = Action {
    implicit request =>
      AsyncResult(
        OpenID.verifiedId.map(userInfo => {
          val provider = userInfo.id.split('?')(0)
          val id = userInfo.id.split('?')(1)
          val email = userInfo.attributes("email")
          val firstname = userInfo.attributes("firstname")
          val lastname = userInfo.attributes("lastname")

          taskCommandHandler ! RegisterUserCommand(provider, id, email, firstname, lastname)
          taskCommandHandler ! LoginUserCommand(email)
          Redirect(routes.Application.index(section = "")).withSession(session + ("email" -> email))
        }
        )
      )
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
