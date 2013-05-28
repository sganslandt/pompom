package model.api

abstract class DomainEvent {
  def aggregateIdentifier: String
}

abstract class UserEvent extends DomainEvent {
  def userId: String
  override def aggregateIdentifier = userId
}

case class UserRegisteredEvent(override val userId: String) extends UserEvent
case class UserLoggedInEvent(override val userId: String) extends UserEvent

case class TaskCreatedEvent(override val userId: String, taskId: String, title: String, description: String, initialEstimate: Int) extends UserEvent
case class TaskDoneEvent(override val userId: String, taskId: String) extends UserEvent
case class EstimateExtendedEvent(override val userId: String, taskId: String, extension: Int) extends UserEvent

case class PomodoroStartedEvent(override val userId: String, taskId: String, pomodoro: Int) extends UserEvent
case class PomodoroEndedEvent(override val userId: String, taskId: String, pomodoro: Int) extends UserEvent
case class PomodoroBrokenEvent(override val userId: String, taskId: String, pomodoro: Int, note: String) extends UserEvent
case class PomodoroInterruptedEvent(override val userId: String, taskId: String, pomodoro: Int, note: String) extends UserEvent







