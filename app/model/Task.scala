package model

import java.util.UUID
import org.joda.time.DateTime
import akka.actor.Actor
import model.api.{TaskCreatedEvent, CreateTaskCommand}

class Task(val id: String, val title: String, val description: String, val initialEstimate: Int) extends Actor {

  var estimate = initialEstimate
  var pomodoros: List[Pomodoro] = List()
  var isDone = false

  def receive = {
    case c: CreateTaskCommand => self ! TaskCreatedEvent(c.userId, id, title, description, initialEstimate)

    case e: TaskCreatedEvent => context.parent ! e
  }

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
    if (isDone)
      throw new IllegalStateException("Can't start a pomodoro on Task that is done.")
    if (inPomodoro)
      throw new IllegalStateException("Can't start a new pomodoro while there is already one active.")
    if (pomodoros.length == estimate)
      throw new IllegalStateException("All estimated pomodoros used, re-estimate.")

    pomodoros = new Pomodoro :: pomodoros
  }

  def endPomodoro() {
    if (!inPomodoro)
      throw new IllegalStateException("No current pomodoro to end.")

    pomodoros.head.end()
  }

  def interrupt(note: String) {
    pomodoros.head.interrupt(note)
  }

  def break() {
    if (!inPomodoro)
      throw new IllegalStateException("No current pomodoro to register an interruption in.")

    pomodoros.head.break()
  }

  def extendEstimate(additionalEstimate: Int) {
    estimate += additionalEstimate
  }

  def done() {
    if (inPomodoro)
      throw new IllegalStateException("Can't finnish a task while in a Pomodoro. Finnish the current pomodoro first.")
    if (isDone)
      throw new IllegalStateException("The task is already done.")

    isDone = true
  }

  case class Interruption(what: String, when: DateTime)

  class Pomodoro() {

    var isActive = true
    var broken = false
    var interruptions: List[Interruption] = List()

    def end() {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not activet and can't be ended.")
      isActive = false
    }

    def interrupt(note: String) {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not active and can't be interrupted.")
      interruptions ::= Interruption(note, DateTime.now)
    }

    def break() {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not active and can't be broken.")
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
