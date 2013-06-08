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
    case _: LoginUserCommand => eventStore ! UserLoggedInEvent(userId)
    case c: CreateTaskCommand => eventStore ! TaskCreatedEvent(c.userId, c.taskId, c.title, c.initialEstimate)
    case c: StartPomodoroCommand => {
      log.debug("Received StartPomodoroCommand")
      eventStore ! prioritizedTaskList.head.startPomodoro()
    }
    case c: EndPomodoroCommand => {
      log.debug("Received EndPomodoroCommand")
      eventStore ! prioritizedTaskList.head.endPomodoro()
    }
    case c: InterruptPomodoroCommand => {
      log.debug("Received InterruptPomodoroCommand")
      eventStore ! prioritizedTaskList.head.interrupt(c.note)
    }
    case c: BreakPomodoroCommand => {
      log.debug("Received BreakPomodoroCommand")
      eventStore ! prioritizedTaskList.head.break()
    }
    case c: ReprioritizeTaskCommand => {
      log.debug("Received {}", c)
      eventStore ! TaskReprioritzedEvent(userId, c.taskId, c.newPriority)
    }


    case e: TaskCreatedEvent => {
      prioritizedTaskList = prioritizedTaskList :+ new Task(userId, e.taskId, e.title, prioritizedTaskList.length, e.initialEstimate)
    }

    case e: TaskReprioritzedEvent => {
      val task = prioritizedTaskList.find(_.taskId == e.taskId)
      task match {
        case
          Some(taskToUpdate) => {
          val oldPriority = taskToUpdate.priority
          val newPriority = e.newPriority

          prioritizedTaskList = rePrioritize(taskToUpdate, prioritizedTaskList, newPriority).toList
        }
        case None =>
      }
    }

    case e: PomodoroStartedEvent => for (t <- prioritizedTaskList.find(_.taskId == e.taskId)) t.apply(e)
    case e: PomodoroEndedEvent => for (t <- prioritizedTaskList.find(_.taskId == e.taskId)) t.apply(e)
    case e: PomodoroInterruptedEvent => for (t <- prioritizedTaskList.find(_.taskId == e.taskId)) t.apply(e)
    case e: PomodoroBrokenEvent => for (t <- prioritizedTaskList.find(_.taskId == e.taskId)) t.apply(e)

    case _ => {}
  }
}

class Settings {
  private var pomodoroLength: Int = 25
}
