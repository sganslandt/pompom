package model

import model.api._
import model.api.PomodoroInterruptedEvent
import model.api.PomodoroEndedEvent
import model.api.PomodoroBrokenEvent
import model.api.PomodoroStartedEvent

class Task(val userId: String, val id: String, val title: String, val description: String, val initialEstimate: Int) {

  var estimate = initialEstimate
  var pomodoros: List[Pomodoro] = List()
  var isDone = false

  private def inPomodoro = {
    pomodoros.nonEmpty && pomodoros.head.isActive
  }

  def startPomodoro(): PomodoroStartedEvent = {
    if (isDone)
      throw new IllegalStateException("Can't start a pomodoro on Task that is done.")
    if (inPomodoro)
      throw new IllegalStateException("Can't start a new pomodoro while there is already one active.")
    if (pomodoros.length == estimate)
      throw new IllegalStateException("All estimated pomodoros used, re-estimate.")

    PomodoroStartedEvent(userId, id, pomodoros.length)
  }

  def endPomodoro() {
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

  def apply(e: DomainEvent) {
    e match {
      case e: PomodoroStartedEvent => {
        pomodoros = new Pomodoro :: pomodoros
      }
      case e: PomodoroEndedEvent => pomodoros.head.apply(e)
      case e: PomodoroBrokenEvent => pomodoros.head.apply(e)
      case _ => {}
    }
  }

  class Pomodoro() {

    var isActive = true

    def end() {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not activet and can't be ended.")
      PomodoroEndedEvent(userId, id, pomodoros.length - 1)
    }

    def interrupt(note: String) {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not active and can't be interrupted.")
      PomodoroInterruptedEvent(userId, id, pomodoros.length - 1, note)
    }

    def break() {
      if (!isActive)
        throw new IllegalStateException("The pomodoro is not active and can't be broken.")
      PomodoroBrokenEvent(userId, id, pomodoros.length - 1, "")
    }

    def apply(e: DomainEvent) {
      e match {
        case _: PomodoroEndedEvent => isActive = false
        case _: PomodoroBrokenEvent => isActive = false
      }
    }
  }

}
