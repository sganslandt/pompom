package model.api

abstract class TaskEvent {
  def taskId: String
}

case class TaskCreatedEvent(override val taskId: String, title: String, description: String, initialEstiamet: Int) extends TaskEvent
case class TaskDoneEvent(override val taskId: String) extends TaskEvent
case class EstimateExtendedEvent(taskId: String, extension: Int)

case class PomodoroStartedEvent(taskId: String, pomodoro: Int)
case class PomodoroEndedEvent(taskId: String, pomodoro: Int)
case class PomodoroBrokenEvent(taskId: String, pomodoro: Int, note: String)
case class PomodoroInterruptedEvent(taskId: String, pomodoro: Int, note: String)







