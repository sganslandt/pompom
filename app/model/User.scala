package model

import akka.actor.{ActorLogging, ActorRef, Actor}
import model.api._
import model.api.UserLoggedInEvent
import model.api.LoginUserCommand
import util.Prioritizable.rePrioritize

class User(userId: String, eventStore: ActorRef) extends Actor with ActorLogging {

  private var prioritizedTaskList: List[Task] = List()
  private val settings: Settings = new Settings()

  eventStore ! Replay(Some(userId))

  def receive = {

    /**
     * Commands
     */

    case _: LoginUserCommand => eventStore ! UserLoggedInEvent(userId)
    case c: CreateTaskCommand => eventStore ! TaskCreatedEvent(c.userId, c.taskId, c.title, c.initialEstimate)
    case c: StartPomodoroCommand => eventStore ! prioritizedTaskList.head.startPomodoro()
    case c: EndPomodoroCommand => eventStore ! prioritizedTaskList.head.endPomodoro()
    case c: InterruptPomodoroCommand => eventStore ! prioritizedTaskList.head.interrupt(c.note)
    case c: BreakPomodoroCommand => eventStore ! prioritizedTaskList.head.break()
    case c: ReprioritizeTaskCommand => eventStore ! TaskReprioritzedEvent(userId, c.taskId, c.newPriority)

    /**
     * Events
     */

    case e: TaskCreatedEvent => {
      prioritizedTaskList = prioritizedTaskList :+ new Task(userId, e.taskId, e.title, prioritizedTaskList.length, e.initialEstimate)
    }

    case e: TaskReprioritzedEvent => {
      for (t <- prioritizedTaskList.find(_.taskId == e.taskId))
        prioritizedTaskList = rePrioritize(t, prioritizedTaskList, e.newPriority).toList
    }

    case e: PomodoroStartedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroEndedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroInterruptedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroBrokenEvent => withTask(e.taskId, _.apply(e))

    case _ => {}
  }

  private def withTask(taskId: String, f: Task => Unit) {
    for (t <- prioritizedTaskList.find(_.taskId == taskId)) f(t)
  }
}

class Settings {
  private var pomodoroLength: Int = 25
}
