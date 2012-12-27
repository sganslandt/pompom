package controllers

import play.api.mvc.{Action, Controller}
import controllers.Authentication.Secured
import model.Task

object Tasks extends Controller with Secured {

  // Application

  def index = AsAuthenticatedUser({
    userId => Action {
      Ok("Application index")
    }
  })

  // API

  def createTask = AsAuthenticatedUser {
    _ =>
      Action {
        Ok("Create new taks!")
      }
  }

  def doCreateTask() = AsAuthenticatedUser({
    userId =>
      Action {
        Task.createTask("abc", "abc", 3, "abc")
        Ok("Task created.")
      }
  })

  def listTasks = AsAuthenticatedUser({
    userId =>
      Action {
        Ok(views.html.tasks.tasklist(Task.listForUser(userId)))
      }
  })

  def getTask(taskId: String) = AsAuthenticatedUser({
    userId => Action {
      val task: Option[Task] = Task.getTask(userId, taskId)
      task match {
        case Some(t) => Ok(views.html.tasks.task(t))
        case None => NotFound("No task with id {"+taskId+"} found.")
      }

    }
  })

  def done(taskId: String) = TODO

  def interrupt(taskId: String) = TODO

  def extendEstimate(taskId: String) = TODO

}
