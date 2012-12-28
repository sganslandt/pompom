package model

import java.util.UUID

case class Task(id: String, title: String, description: String, initialEstimate: Int) {
  var done: Boolean = false
  var current: Boolean = false
}

object Task {

  type User = String

  val EMPTY: List[Task] = List(Task("id", "title", "desc", 3))

  var tasks: Map[User, List[Task]] = Map()

  def createTask(userId: User, title: String, initialEstimate: Int, description: String): String = {
    val id = UUID.randomUUID().toString
    val userTasks = tasks.get(userId).getOrElse(EMPTY)
    val newTask: Task = new Task(id, title, description, initialEstimate)
    val newEntry: (User, List[Task]) = (userId, newTask :: userTasks)
    tasks = tasks + newEntry

    id
  }

  def getTask(userId: User, taskId: String): Option[Task] = {
    val tasksForUser = tasks.get(userId).getOrElse(EMPTY)
    tasksForUser.find({
      task => task.id == taskId
    })
  }

  def listForUser(userId: String): Seq[Task] = {
    tasks.get(userId).getOrElse(EMPTY)
  }


}
