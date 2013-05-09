package model.api

case class RegisterUserCommand()
case class LoginUserCommand(userId: String)

case class CreateTaskCommand(userId: String, taskId: String, title: String, description: String, initialEstimate: Int)

case class StartPomodoroCommand(taskId: String)
case class EndPomodoroCommand(taskId: String)
case class InterruptPomodoroCommand(taskId: String, note: String)
case class BreakPomodoroCommand(taskId: String, note: String)

case class ExtendEstimateCommand(taskId: String, extension: Int)
case class CompleteTask(taskId: String)
