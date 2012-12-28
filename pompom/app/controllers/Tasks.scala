package controllers

import play.api.mvc.{Action, Controller}
import controllers.Authentication.Secured
import model.Task
import play.api.data._
import play.api.data.Forms._

object Tasks extends Controller with Secured {

  // API

  val createTaskForm = Form(
    tuple(
      "title" -> text,
      "initialEstimate" -> number(1, 12),
      "description" -> text
    )
  )

  def createTask = AsAuthenticatedUser {
    _ =>
      Action {
        Ok(views.html.tasks.create())
      }
  }

  def doCreateTask() = AsAuthenticatedUser(
    userId =>
      Action {
        implicit request =>
          createTaskForm.bindFromRequest.fold(
          errors => BadRequest("There where errors"), {
            case (title, initialEstimate, description) =>
              val taskId = Task.createTask(userId, title, initialEstimate, description)
              Created(taskId)
          }
          )
      }
  )

  def listTasks = AsAuthenticatedUser({
    userId =>
      Action {
        Ok(views.html.tasks.list(Task.listForUser(userId)))
      }
  })

  def getTask(taskId: String) = AsAuthenticatedUser({
    userId => Action {
      val task: Option[Task] = Task.getTask(userId, taskId)
      task match {
        case Some(t) => Ok(views.html.tasks.task(t))
        case None => NotFound("No task with id {" + taskId + "} found.")
      }

    }
  })

  def done(taskId: String) = TODO

  def interrupt(taskId: String) = TODO

  def extendEstimate(taskId: String) = TODO

}
