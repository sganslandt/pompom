package model.api

abstract class DomainEvent{
  def aggregateIdentifier: String
}

abstract class UserEvent extends DomainEvent {
  def userId: String
  def aggregateIdentifier = userId
}

case class UserRegisteredEvent(override val userId: String) extends UserEvent
case class UserLoggedInEvent(override val userId: String) extends  UserEvent

abstract class TaskEvent {
  def taskId: String
}

case class TaskCreatedEvent(override val userId: String, taskId: String, title: String, description: String, initialEstimate: Int) extends UserEvent
case class TaskDoneEvent(override val taskId: String) extends TaskEvent
case class EstimateExtendedEvent(taskId: String, extension: Int)

case class PomodoroStartedEvent(taskId: String, pomodoro: Int)
case class PomodoroEndedEvent(taskId: String, pomodoro: Int)
case class PomodoroBrokenEvent(taskId: String, pomodoro: Int, note: String)
case class PomodoroInterruptedEvent(taskId: String, pomodoro: Int, note: String)







