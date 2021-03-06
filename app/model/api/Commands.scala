package model.api

case class RegisterUserCommand(userId: String, idProvider: String, id: String, email: String, firstname: String, lastname: String)
case class LoginUserCommand(userId: String)

case class CreateTaskCommand(userId: String, taskId: String, title: String, initialEstimate: Int, list: ListType)
case class ReprioritizeTaskCommand(userId: String, taskId: String, newPriority: Int)
case class MoveTaskToListCommand(userId: String, taskId: String, newList: ListType)

case class StartPomodoroCommand(userId: String, taskId: String)
case class EndPomodoroCommand(userId: String, taskId: String)
case class InterruptPomodoroCommand(userId: String, taskId: String, note: String)
case class BreakPomodoroCommand(userId: String, taskId: String, note: String)

case class ExtendEstimateCommand(userId: String, taskId: String, extension: Int)
case class CompleteTaskCommand(userId: String, taskId: String)
