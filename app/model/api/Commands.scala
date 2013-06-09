package model.api

case class RegisterUserCommand()
case class LoginUserCommand(userId: String)

case class CreateTaskCommand(userId: String, taskId: String, title: String, initialEstimate: Int, list: ListType)
case class ReprioritizeTaskCommand(userId: String, taskId: String, newPriority: Int)
case class MoveTaskToListCommand(userId: String, taskId: String, newList: ListType)

case class StartPomodoroCommand(userId: String)
case class EndPomodoroCommand(userId: String)
case class InterruptPomodoroCommand(userId: String, note: String)
case class BreakPomodoroCommand(userId: String, note: String)

case class ExtendEstimateCommand(userId: String, taskId: String, extension: Int)
case class CompleteTaskCommand(userId: String)
