package model

import akka.actor.{Props, ActorLogging, ActorRef, Actor}
import model.api._
import model.api.UserLoggedInEvent
import model.api.LoginUserCommand
import play.api.libs.concurrent.Akka
import play.api.Play.current

class User(userId: String) extends Actor with ActorLogging {

  private var prioritizedTaskList: List[Task] = List()
  private val settings: Settings = new Settings()

  val eventStore = Akka.system.actorFor("/user/eventStore")
  eventStore ! Replay(Some(userId))

  def receive = {
    case _: LoginUserCommand => eventStore ! UserLoggedInEvent(userId)
    case c: CreateTaskCommand => eventStore ! TaskCreatedEvent(c.userId, c.taskId, c.title, c.description, c.initialEstimate)
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

    case e: TaskCreatedEvent => {
      prioritizedTaskList = new Task(userId, e.taskId, e.title, e.description, e.initialEstimate) :: prioritizedTaskList
    }
    case e: PomodoroStartedEvent => for (t <- prioritizedTaskList.find(_.id == e.taskId)) t.apply(e)
    case e: PomodoroEndedEvent => for (t <- prioritizedTaskList.find(_.id == e.taskId)) t.apply(e)
    case e: PomodoroInterruptedEvent => for (t <- prioritizedTaskList.find(_.id == e.taskId)) t.apply(e)
    case e: PomodoroBrokenEvent => for (t <- prioritizedTaskList.find(_.id == e.taskId)) t.apply(e)

    case _ => {}
  }
}

class Settings {
  private var pomodoroLength: Int = 25
}
