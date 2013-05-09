package controllers

import play.api.mvc.{Result, Controller}
import controllers.Authentication.Secured
import model.Task
import play.api.data._
import play.api.data.Forms._
import akka.actor.ActorRef
import model.api.CreateTaskCommand
import java.util.UUID
import play.api.libs.concurrent.Akka
import play.api.Play.current

object Tasks extends Controller with Secured {

  val taskCommandHandler: ActorRef = Akka.system.actorFor("/user/taskCommandHandler")

  val createTaskForm = Form(
    tuple(
      "title" -> text(1),
      "initialEstimate" -> number(1, 12),
      "description" -> text(0)
    )
  )

  def createTask() = AsAuthenticatedUser(
    userId => {
      implicit request =>
        createTaskForm.bindFromRequest.fold(
        form => Forbidden(""), {
          case (title, initialEstimate, description) =>
            taskCommandHandler ! CreateTaskCommand(userId, UUID.randomUUID().toString, title, description, initialEstimate)
            Ok("")
        })
    }
  )

  def startPomodoro(taskId: String) = AsAuthenticatedUser {
    userId => {
      _ =>
        withTask(userId, taskId, {
          task =>
            task.startPomodoro()
            Ok("")
        })
    }
  }

  def endPomodoro(taskId: String) = AsAuthenticatedUser {
    userId => {
      _ =>
        withTask(userId, taskId, {
          task =>
            task.endPomodoro()
            Ok("")
        })
    }
  }

  val interruptForm = Form(
    single(
      "note" -> text(3)
    )
  )

  def interrupt(taskId: String) = AsAuthenticatedUser {
    userId => {
      implicit request =>
        withTask(userId, taskId, {
          task =>
            interruptForm.bindFromRequest.fold(
              errors => Forbidden("Validation errors."),
              value => {
                task.interrupt(value)
                Ok("")
              }
            )
        })
    }
  }

  def break(taskId: String) = AsAuthenticatedUser {
    userId => {
      _ =>
        withTask(userId, taskId, {
          task =>
            task.break()
            Ok("")
        })
    }
  }

  val extendEstimateForm = Form(
    single(
      "additionalPomodoros" -> number(1, 12)
    )
  )

  def extendEstimate(taskId: String) = AsAuthenticatedUser {
    userId => {
      implicit request =>
        withTask(userId, taskId, {
          task =>
            extendEstimateForm.bindFromRequest.fold(
              errors => Forbidden("Validation"),
              value => {
                task.extendEstimate(value)
                Ok("")
              }
            )
        })
    }
  }

  def complete(taskId: String) = AsAuthenticatedUser {
    userId => {
      _ =>
        withTask(userId, taskId, {
          task =>
            task.done()
            Ok("")
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
          case e: IllegalStateException => Forbidden("")
        }
      }
      case None => NotFound("404 - Task not found")
    }
  }

}
