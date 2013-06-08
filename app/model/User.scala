package model

import akka.actor.{ActorLogging, ActorRef, Actor}
import model.api._
import model.api.UserLoggedInEvent
import model.api.LoginUserCommand
import util.Prioritizable.rePrioritize

class User(userId: String, eventStore: ActorRef) extends Actor with ActorLogging {

  private var todoToday: List[Task] = List()
  private var activityInventory: List[Task] = List()
  private val settings: Settings = new Settings()

  eventStore ! Replay(Some(userId))

  def receive = {

    /**
     * Commands
     */

    case _: LoginUserCommand => eventStore ! UserLoggedInEvent(userId)
    case c: CreateTaskCommand => eventStore ! TaskCreatedEvent(c.userId, c.taskId, c.title, c.initialEstimate, activityInventory.size, ActivityInventory)
    case c: StartPomodoroCommand => eventStore ! todoToday.head.startPomodoro()
    case c: EndPomodoroCommand => eventStore ! todoToday.head.endPomodoro()
    case c: InterruptPomodoroCommand => eventStore ! todoToday.head.interrupt(c.note)
    case c: BreakPomodoroCommand => eventStore ! todoToday.head.break()
    case c: ReprioritizeTaskCommand => eventStore ! TaskReprioritzedEvent(userId, c.taskId, c.newPriority)

    /**
     * Events
     */

    case e: TaskCreatedEvent => {
      e.list match {
        case TodoToday => todoToday = todoToday :+ new Task(userId, e.taskId, e.title, todoToday.length, e.initialEstimate)
        case ActivityInventory => activityInventory = activityInventory :+ new Task(userId, e.taskId, e.title, activityInventory.length, e.initialEstimate)
      }

    }

    case e: TaskReprioritzedEvent => {
      for (t <- getTask(e.taskId))
        if (todoToday.contains(t)) // no need to keep activityInventory sorted here
          todoToday = rePrioritize(t, todoToday, e.newPriority).toList
    }

    case e: PomodoroStartedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroEndedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroInterruptedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroBrokenEvent => withTask(e.taskId, _.apply(e))

    case _ => {}
  }

  private def getTask(taskId: String): Option[Task] = {
    var task = todoToday.find(_.taskId == taskId)
    if (task == None)
      task = activityInventory.find(_.taskId == taskId)

    task
  }

  private def withTask(taskId: String, f: Task => Unit) {
    for (t <- todoToday.find(_.taskId == taskId)) f(t)
    for (t <- activityInventory.find(_.taskId == taskId)) f(t)
  }
}

class Settings {
  private var pomodoroLength: Int = 25
}
