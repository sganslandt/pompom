package model

import java.util.UUID

case class Task(id: String, title: String, description: String, initialEstimate: Int)

object Task {

  type User = String

  var tasks: Map[User, List[Task]] = Map()

  def createTask(userId: User, title: String, initialEstimate: Int, description: String): String =  {
    val id = UUID.randomUUID().toString
    val userTasks = tasks(userId)
    val newTask: Task = new Task(id, title, description, initialEstimate)
    val newEntry: (User, List[Task]) = (userId, newTask :: userTasks)
    tasks = tasks + newEntry

    id
  }

  def getTask(userId: User, taskId: String): Option[Task] = {
    val tasksForUser = tasks(userId)
    tasksForUser.find({task => task.id == taskId})
  }

  def listForUser(userId: String): List[Task] =  {
    tasks(userId)
  }

}
