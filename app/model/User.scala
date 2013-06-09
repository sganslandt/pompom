package model

import akka.actor.{ActorLogging, ActorRef, Actor}
import model.api._
import model.api.UserLoggedInEvent
import model.api.LoginUserCommand
import util.Prioritizable.rePrioritize

class User(userId: String, eventStore: ActorRef) extends Actor with ActorLogging {

  private var lists: Map[ListType, List[Task]] = Map(TodoToday -> List(), ActivityInventory -> List())

  private val settings: Settings = new Settings()

  eventStore ! Replay(Some(userId))

  def receive = {

    /**
     * Commands
     */

    case _: LoginUserCommand => eventStore ! UserLoggedInEvent(userId)
    case c: CreateTaskCommand => eventStore ! TaskCreatedEvent(c.userId, c.taskId, c.title, c.initialEstimate, lists(c.list).size, c.list)
    case c: StartPomodoroCommand => eventStore ! lists(TodoToday).head.startPomodoro()
    case c: EndPomodoroCommand => eventStore ! lists(TodoToday).head.endPomodoro()
    case c: InterruptPomodoroCommand => eventStore ! lists(TodoToday).head.interrupt(c.note)
    case c: BreakPomodoroCommand => eventStore ! lists(TodoToday).head.break()
    case c: ReprioritizeTaskCommand => eventStore ! TaskReprioritzedEvent(userId, c.taskId, c.newPriority)

    /**
     * Events
     */

    case e: TaskCreatedEvent =>
      lists = lists + (e.list -> (lists(e.list) :+ new Task(userId, e.taskId, e.title, lists(e.list).length, e.initialEstimate)))

    case e: TaskReprioritzedEvent => {
      for (t <- getTask(e.taskId))
        if (lists(TodoToday).contains(t)) // no need to keep activityInventory sorted here
          lists = lists + (TodoToday -> rePrioritize(t, lists(TodoToday), e.newPriority).toList)
    }

    case e: PomodoroStartedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroEndedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroInterruptedEvent => withTask(e.taskId, _.apply(e))
    case e: PomodoroBrokenEvent => withTask(e.taskId, _.apply(e))

    case _ => {}
  }

  private def getTask(taskId: String): Option[Task] = {
    lists.flatten(_._2).find(_.taskId == taskId)
  }

  private def withTask(taskId: String, f: Task => Unit) {
    for (t <- lists.flatten(_._2).find(_.taskId == taskId)) f(t)
  }
}

class Settings {
  private var pomodoroLength: Int = 25
}
