package model

import java.util.UUID
import org.joda.time.DateTime

class Task(val id: String, val title: String, val description: String, val initialEstimate: Int) {

  var estimate = initialEstimate
  var pomodoros: List[Pomodoro] = List()
  var isDone = false

  def inPomodoro = {
    pomodoros.nonEmpty && pomodoros.head.isActive
  }

  def interruptions = {
    pomodoros.foldLeft(List[Interruption]())({
      (is, task) => is ++ task.interruptions
    })
  }

  def usedPomodoros = {
    if (inPomodoro) pomodoros.length - 1
    else pomodoros.length
  }

  def startPomodoro() {
    if (isDone || inPomodoro || pomodoros.length == estimate)
      throw new IllegalStateException()

    pomodoros = new Pomodoro :: pomodoros
  }

  def endPomodoro() {
    if (!inPomodoro)
      throw new IllegalStateException()

    pomodoros.head.end()
  }

  def interrupt(what: String) {
    pomodoros.head.interrupt(what)
  }

  def break() {
    if (!inPomodoro)
      throw new IllegalStateException()

    pomodoros.head.break()
  }

  def extendEstimate(additionalEstimate: Int) {
    estimate += additionalEstimate
  }

  def done() {
    if (inPomodoro || isDone)
      throw new IllegalStateException()

    isDone = true
  }

  case class Interruption(what: String, when: DateTime)

  class Pomodoro() {

    var isActive = true
    var broken = false
    var interruptions: List[Interruption] = List()

    def end() {
      if (!isActive)
        throw new IllegalStateException()
      isActive = false
    }

    def interrupt(what: String) {
      if (!isActive)
        throw new IllegalStateException()
      interruptions ::= Interruption(what, DateTime.now)
    }

    def break() {
      if (!isActive)
        throw new IllegalStateException()
      isActive = false
    }

  }

}

object Task {

  type User = String

  val EMPTY: List[Task] = List(new Task("id", "title", "desc", 3))

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
