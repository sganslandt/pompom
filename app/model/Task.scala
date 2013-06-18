package model

import model.api._
import model.api.PomodoroInterruptedEvent
import model.api.PomodoroEndedEvent
import model.api.PomodoroBrokenEvent
import model.api.PomodoroStartedEvent
import util.Prioritizable

class Task(val userId: String, val taskId: String, val title: String, var priority: Int, val initialEstimate: Int, var list: ListType) extends Prioritizable[Task] {

  var estimate = initialEstimate
  var pomodoros: List[Pomodoro] = List()
  var isDone = false

  def setPriority(newPriority: Int) = { this.priority = priority; this }
  def increasePriority = { this.priority += 1; this }
  def decreasePriority = { this.priority -= 1; this }

  private def inPomodoro = {
    pomodoros.nonEmpty && pomodoros.head.isActive
  }

  def startPomodoro() = {
    if (isDone)
      throw new IllegalStateException("Can't start a pomodoro on Task that is done.")
    if (inPomodoro)
      throw new IllegalStateException("Can't start a new pomodoro while there is already one active.")
    if (pomodoros.length == estimate)
      throw new IllegalStateException("All estimated pomodoros used, re-estimate.")

    PomodoroStartedEvent(userId, taskId, pomodoros.length)
  }

  def endPomodoro() = {
    pomodoros.head.end()
  }

  def interrupt(note: String) = {
    pomodoros.head.interrupt(note)
  }

  def break() = {
    if (!inPomodoro)
      throw new IllegalStateException("No current pomodoro to register an interruption in.")

    pomodoros.head.break()
  }

  def extendEstimate(additionalEstimate: Int) {
    estimate += additionalEstimate
  }

  def complete() = {
    if (inPomodoro)
      throw new IllegalStateException("Can't finnish a task while in a Pomodoro. Finnish the current pomodoro first.")
    if (isDone)
      throw new IllegalStateException("The task is already done.")

    new TaskCompletedEvent(userId, taskId)
  }

  def apply(e: DomainEvent) {
    e match {
      case e: PomodoroStartedEvent => {
        pomodoros = new Pomodoro :: pomodoros
      }
      case e: PomodoroEndedEvent => pomodoros.head.apply(e)
      case e: PomodoroBrokenEvent => pomodoros.head.apply(e)
      case e: TaskCompletedEvent => isDone = true
      case _ => {}
    }
  }

  class Pomodoro() {

    var isActive = true

    def end() = {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not activet and can't be ended.")
      PomodoroEndedEvent(userId, taskId, pomodoros.length - 1)
    }

    def interrupt(note: String) = {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not active and can't be interrupted.")
      PomodoroInterruptedEvent(userId, taskId, pomodoros.length - 1, note)
    }

    def break() = {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not active and can't be broken.")
      PomodoroBrokenEvent(userId, taskId, pomodoros.length - 1, "")
    }

    def apply(e: DomainEvent) {
      e match {
        case _: PomodoroEndedEvent => isActive = false
        case _: PomodoroBrokenEvent => isActive = false
      }
    }
  }

}
