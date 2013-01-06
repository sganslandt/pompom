package controllers

import play.api.mvc.{Result, Action, Controller}
import controllers.Authentication.Secured
import model.Task
import play.api.data._
import play.api.data.Forms._

object Tasks extends Controller with Secured {

  val createTaskForm = Form(
    tuple(
      "title" -> text(3),
      "initialEstimate" -> number(1, 12),
      "description" -> text(3)
    )
  )

  def createTask() = AsAuthenticatedUser(
    userId =>
      Action {
        implicit request =>
          createTaskForm.bindFromRequest.fold(
          errors => Forbidden("Validation errors."), {
            case (title, initialEstimate, description) =>
              val taskId = Task.createTask(userId, title, initialEstimate, description)
              Created(views.html.tasks.link("Created", taskId))
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
      withTask(userId, taskId, {
        t => Ok(views.html.tasks.task(t))
      })
    }
  }

  def startPomodoro(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withTask(userId, taskId, {
        task =>
            task.startPomodoro()
            Accepted(views.html.tasks.link("Pomodoro started", task.id))
      })
    }
  }

  def endPomodoro(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withTask(userId, taskId, {
        task =>
            task.endPomodoro()
            Accepted(views.html.tasks.link("Pomodoro ended", task.id))
      })
    }
  }

  val interruptForm = Form(
    single(
      "note" -> text(3)
    )
  )

  def interrupt(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      implicit request =>
        withTask(userId, taskId, {
          task =>
            interruptForm.bindFromRequest.fold(
              errors => Forbidden("Validation errors."),
              value => {
                  task.interrupt(value)
                  Accepted(views.html.tasks.link("Interruption recorded", task.id))
              }
            )
        })
    }
  }

  def break(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withTask(userId, taskId, {
        task =>
            task.break()
            Accepted(views.html.tasks.link("Pomodoro broken", task.id))
      })
    }
  }

  val extendEstimateForm = Form(
    single(
      "additionalPomodoros" -> number(1, 12)
    )
  )

  def extendEstimate(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      implicit request =>
        withTask(userId, taskId, {
          task =>
            extendEstimateForm.bindFromRequest.fold(
              errors => Forbidden("Validation"),
              value => {
                task.extendEstimate(value)
                Accepted(views.html.tasks.link("Estimate extended", task.id))
              }
            )
        })
    }
  }

  def complete(taskId: String) = AsAuthenticatedUser {
    userId => Action {
      withTask(userId, taskId, {
        task =>
          task.done()
          Accepted(views.html.tasks.link("Task completed", task.id))
      })
    }
  }

  def withTask(userId: String, taskId: String, f: Task => Result): Result = {
    val task = Task.getTask(userId, taskId)
    task match {
      case Some(t) => {
        try {
          f(t)
        } catch {
          case e: IllegalStateException => Forbidden(views.html.tasks.link("Illegal state", t.id))
        }
      }
      case None => NotFound("404 - Task not found")
    }
  }

}
