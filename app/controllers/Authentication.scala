package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.ActorRef
import model.api.{RegisterUserCommand, LoginUserCommand}
import scala.concurrent.ExecutionContext
import play.api.libs.openid.OpenID
import ExecutionContext.Implicits.global
import views.{User, TaskQueryRepository}
import java.util.UUID

object Authentication extends Controller {

  val taskCommandHandler: ActorRef = Akka.system.actorFor("/user/taskCommandHandler")

  var taskQueryRepository: Option[TaskQueryRepository] = None
  def setTaskQueryRepository(repository: TaskQueryRepository) {
    Authentication.taskQueryRepository = Some(repository)
  }

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

          taskQueryRepository match {
            case Some(repo) => {
              val user: Option[User] = repo.getUserByEmail(email)
              user match {
                case None => {
                  val userId = UUID.randomUUID().toString()
                  taskCommandHandler ! RegisterUserCommand(userId, provider, id, email, firstname, lastname)
                  Redirect(routes.Application.index(section = "")).withSession(session + ("userId" -> userId))
                }
                case Some(user) => {
                  taskCommandHandler ! LoginUserCommand(user.userId)
                  Redirect(routes.Application.index(section = "")).withSession(session + ("userId" -> user.userId))
                }
              }
            }
            case None => ServiceUnavailable("")
          }
        }
        )
      )
  }

  /**
   * Provide security features
   */
  trait Secured {

    /**
     * Retrieve the connected users id.
     */
    private def userId(request: RequestHeader) = request.session.get("userId")

    /**
     * Redirect to login if the user in not authorized.
     */
    private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.login())

    // --

    /**
     * Action for authenticated users.
     */
    def AsAuthenticatedUser(f: => String => Request[AnyContent] => Result) = Security.Authenticated(userId, onUnauthorized) {
      user => {
        taskQueryRepository match {
          case None => Action(ServiceUnavailable(""))
          case Some(repo) => {
            if (repo.isUserRegistered(user))
              Action(request => f(user)(request))
            else
              Action(Results.Redirect(routes.Authentication.login()))
          }
        }
      }
    }

  }
}
