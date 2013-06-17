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
import views.{Pomodoro, Task}
import org.joda.time.DateTime

object Tasks extends Controller with Secured {

  val taskCommandHandler: ActorRef = Akka.system.actorFor("/user/taskCommandHandler")

  val createTaskForm = Form(
    tuple(
      "title" -> text(1),
      "initialEstimate" -> number(1, 8),
      "listName" -> text()
    )
  )

  def createTask() = AsAuthenticatedUser(
    userId => {
      implicit request =>
        createTaskForm.bindFromRequest.fold(
        form => Forbidden(""), {
          case (title, initialEstimate, listName) =>
            val listType = ListType.fromString(listName)
            var newTaskId = UUID.randomUUID().toString
            listType match {
              case Some(listType) => taskCommandHandler ! CreateTaskCommand(userId, newTaskId, title, initialEstimate, listType); Created(views.html.tasks.task(Task(userId, newTaskId, title, Pomodoro(initialEstimate), -1, DateTime.now(), None, listType)))
              case None => BadRequest("")
            }
        })
    }
  )

  val reprioritizeTaskForm = Form(
    tuple(
      "taskId" -> text(),
      "newPriority" -> number()
    )
  )

  def reprioritizeTask() = AsAuthenticatedUser(
    userId => {
      implicit request =>
        reprioritizeTaskForm.bindFromRequest.fold(
        form => Forbidden(""), {
          case (taskId, newPriority) =>
            taskCommandHandler ! ReprioritizeTaskCommand(userId, taskId, newPriority)
            Ok("")
        })
    }
  )

  val moveTaskToListForm = Form(
    tuple(
      "taskId" -> text(),
      "newList" -> text()
    )
  )

  def moveTaskToList() = AsAuthenticatedUser(
    userId => {
      implicit request =>
        moveTaskToListForm.bindFromRequest.fold(
        form => Forbidden(""), {
          case (taskId, newList) =>
            val listType = ListType.fromString(newList)
            listType match {
              case Some(listType) => taskCommandHandler ! MoveTaskToListCommand(userId, taskId, listType); Ok("")
              case None => BadRequest("")
            }
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

  def interruptPomodoro() = AsAuthenticatedUser {
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

  def breakPomodoro() = AsAuthenticatedUser {
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

  def completeTask() = AsAuthenticatedUser {
    userId => {
      _ =>
        taskCommandHandler ! CompleteTaskCommand(userId, "")
        Ok("")
    }
  }

}

