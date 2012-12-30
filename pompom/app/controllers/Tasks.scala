package controllers

import play.api.mvc.{Result, Action, Controller}
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

  def listTasks = AsAuthenticatedUser {
    userId =>
      Action {
        Ok(views.html.tasks.list(Task.listForUser(userId)))
      }
  }

  def getTask(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withOptionalTask(userId, taskId, {
        t => Ok(views.html.tasks.task(t))
      })
    }
  }

  def startPomodoro(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withOptionalTask(userId, taskId, {
        task =>
          task.startPomodoro()
          Accepted("") // TODO Possibly link to task?
      })
    }
  }

  def endPomodoro(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withOptionalTask(userId, taskId, {
        task =>
          task.endPomodoro()
          Accepted("") // TODO Possibly link to task?
      })
    }
  }

  val interruptForm = Form(
    single(
      "what" -> text
    )
  )

  def interrupt(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      implicit request =>
        withOptionalTask(userId, taskId, {
          task =>
            interruptForm.bindFromRequest.fold(
              errors => BadRequest("Validation Error"),
              value => {
                task.interrupt(value)
                Accepted("")
              }
            )
        })
    }
  }

  def break(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withOptionalTask(userId, taskId, {
        task =>
          task.break()
          Accepted("")
      })
    }
  }

  val extendEstimateForm = Form(
    single(
      "additionalPomodoros" -> number(1, 12)
    )
  )

  def extendEstimate(taskId: String) = AsAuthenticatedUser {
    userId => Action { implicit request =>
      withOptionalTask(userId, taskId, {
        task =>
          extendEstimateForm.bindFromRequest.fold(
            errors => BadRequest("Validation"),
            value => {
              task.extendEstimate(value)
              Accepted("")
            }
          )
      })
    }
  }

  def done(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withOptionalTask(userId, taskId, {
        task =>
          task.done()
          Accepted("")
      })
    }
  }

  def withOptionalTask(userId: String, taskId: String, f: Task => Result): Result = {
    val task = Task.getTask(userId, taskId)
    task match {
      case Some(t) => f(t)
      case None => NotFound("404 - Task not found")
    }
  }

}
