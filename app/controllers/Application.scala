package controllers

import play.api._
import mvc._
import controllers.Authentication.Secured
import views.TaskQueryRepository

object Application extends Controller with Secured {

  var taskQueryRepository: Option[TaskQueryRepository] = None
  def setTaskQueryRepository(repository: TaskQueryRepository) {
    Application.taskQueryRepository = Some(repository)
  }

  def index(section: String) = AsAuthenticatedUser(userId => {
    request =>
      taskQueryRepository match {
        case Some(repository) =>
          Ok(views.html.index(
            userId,
            repository.listTodoToday(userId),
            repository.listActivityInventory(userId)
          ))
        case None => ServiceUnavailable("")
      }
  })

  def currentAuthenticatedUser = AsAuthenticatedUser {
    userId => {
      request =>
        Ok(views.html.auth.currentUser(userId))
    }
  }

}