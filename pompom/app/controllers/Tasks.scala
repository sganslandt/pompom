package controllers

import play.api.mvc.{Action, Controller}
import controllers.Authentication.Secured
import model.Task

object Tasks extends Controller with Secured {

  // Application

  def index = AsCurrentUser({ userId => Action { Ok("Application index") }})

  // API

  def createTask = AsCurrentUser({ userId =>
    Action {
      Task.createTask("abc", "abc", 3, "abc")
      Ok("Task created.")
    }})

  def listTasks = AsCurrentUser({ userId => Action { Ok("Tasks for user: " + userId) }})

  def getTask(taskId: String) = AsCurrentUser({ userId => Action { Ok("Task {"+taskId+"} for user {"+userId+"}")}})

}
