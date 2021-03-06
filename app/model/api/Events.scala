package model.api

abstract class DomainEvent {
  def aggregateIdentifier: String
}

abstract class UserEvent extends DomainEvent {
  def userId: String
  override def aggregateIdentifier = userId
}

case class UserRegisteredEvent(override val userId: String, idProvider: String, id: String, email: String, firstname: String, lastname: String) extends UserEvent
case class UserLoggedInEvent(override val userId: String) extends UserEvent

case class TaskCreatedEvent(override val userId: String, taskId: String, title: String, initialEstimate: Int, priority: Int, list: ListType) extends UserEvent
case class TaskReprioritzedEvent(override val userId: String, taskId: String, newPriority: Int) extends UserEvent
case class TaskMovedToListEvent(override val userId: String, taskId: String, oldList: ListType, newList: ListType) extends UserEvent
case class TaskCompletedEvent(override val userId: String, taskId: String) extends UserEvent
case class EstimateExtendedEvent(override val userId: String, taskId: String, extension: Int) extends UserEvent

case class PomodoroStartedEvent(override val userId: String, taskId: String, pomodoro: Int) extends UserEvent
case class PomodoroEndedEvent(override val userId: String, taskId: String, pomodoro: Int) extends UserEvent
case class PomodoroBrokenEvent(override val userId: String, taskId: String, pomodoro: Int, note: String) extends UserEvent
case class PomodoroInterruptedEvent(override val userId: String, taskId: String, pomodoro: Int, note: String) extends UserEvent

sealed trait ListType
object ListType {
  def fromString(value: String): Option[ListType] = {
    Vector(TodoToday, ActivityInventory).find(_.toString == value)
  }
}

case object TodoToday extends ListType
case object ActivityInventory extends ListType







