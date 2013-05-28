package controllers

import play.api.mvc.Controller
import controllers.Authentication.Secured
import play.api.data._
import play.api.data.Forms._
import akka.actor.ActorRef
import model.api._
import java.util.UUID
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.api.StartPomodoroCommand
import model.api.CreateTaskCommand
import model.api.EndPomodoroCommand
import model.api.BreakPomodoroCommand

object Tasks extends Controller with Secured {

  val taskCommandHandler: ActorRef = Akka.system.actorFor("/user/taskCommandHandler")

  val createTaskForm = Form(
    tuple(
      "title" -> text(1),
      "initialEstimate" -> number(1, 12)
    )
  )

  def createTask() = AsAuthenticatedUser(
    userId => {
      implicit request =>
        createTaskForm.bindFromRequest.fold(
        form => Forbidden(""), {
          case (title, initialEstimate) =>
            taskCommandHandler ! CreateTaskCommand(userId, UUID.randomUUID().toString, title, "", initialEstimate)
            Ok("")
        })
    }
  )

  def startPomodoro() = AsAuthenticatedUser {
    userId => {
      _ =>
        taskCommandHandler ! new StartPomodoroCommand(userId)
        Ok("")
    }
  }

  def endPomodoro() = AsAuthenticatedUser {
    userId => {
      _ =>
        taskCommandHandler ! EndPomodoroCommand(userId)
        Ok("")
    }
  }

  val interruptForm = Form(
    single(
      "note" -> text(3)
    )
  )

  def interrupt() = AsAuthenticatedUser {
    userId => {
      implicit request =>
        interruptForm.bindFromRequest.fold(
          errors => Forbidden("Validation errors."),
          value => {
            taskCommandHandler ! InterruptPomodoroCommand(userId, value)
            Ok("")
          }
        )
    }
  }

  val breakForm = Form(
    single(
      "note" -> text(3)
    )
  )

  def break() = AsAuthenticatedUser {
    userId => {
      implicit request =>
        breakForm.bindFromRequest.fold(
          errors => Forbidden("Validation errors."),
          value => {
            taskCommandHandler ! BreakPomodoroCommand(userId, value)
            Ok("")
          }
        )
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
        extendEstimateForm.bindFromRequest.fold(
          errors => Forbidden("Validation"),
          value => {
            taskCommandHandler ! ExtendEstimateCommand(userId, taskId, value)
            Ok("")
          }
        )
    }
  }

  def complete() = AsAuthenticatedUser {
    userId => {
      _ =>
        taskCommandHandler ! CompleteTaskCommand(userId)
        Ok("")
    }
  }

}
