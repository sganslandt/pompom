package model

import akka.actor.{ActorLogging, Actor}
import model.api._
import model.api.UserLoggedInEvent
import model.api.LoginUserCommand
import util.Prioritizable.rePrioritize
import org.eligosource.eventsourced.core.Message

class User(userId: String) extends Actor with ActorLogging {

  private var lists: Map[ListType, List[Task]] = Map(TodoToday -> List(), ActivityInventory -> List())

  private val settings: Settings = new Settings()

  override def preStart() {
    log.debug("Starting")
  }

  def receive = {

    /**
     * Commands
     */

    case _: LoginUserCommand => sender ! Message(UserLoggedInEvent(userId))
    case c: CreateTaskCommand => sender ! Message(TaskCreatedEvent(c.userId, c.taskId, c.title, c.initialEstimate, lists(c.list).size, c.list))
    case c: ReprioritizeTaskCommand => sender ! Message(TaskReprioritzedEvent(userId, c.taskId, c.newPriority))
    case c: MoveTaskToListCommand => {
      for (t <- getTask(c.taskId)) sender ! Message(TaskMovedToListEvent(c.userId, c.taskId, t.list, c.newList))
    }

    case c: StartPomodoroCommand => sender ! Message(lists(TodoToday).head.startPomodoro())
    case c: EndPomodoroCommand => sender ! Message(lists(TodoToday).head.endPomodoro())
    case c: InterruptPomodoroCommand => sender ! Message(lists(TodoToday).head.interrupt(c.note))
    case c: BreakPomodoroCommand => sender ! Message(lists(TodoToday).head.break())

    /**
     * Events
     */
    case m: Message => m.event match {
      case e: TaskCreatedEvent =>
        lists = lists + (e.list -> (lists(e.list) :+ new Task(userId, e.taskId, e.title, lists(e.list).length, e.initialEstimate, e.list)))

      case e: TaskReprioritzedEvent => {
        for (t <- getTask(e.taskId))
          if (lists(TodoToday).contains(t)) // no need to keep activityInventory sorted here
            lists = lists + (TodoToday -> rePrioritize(t, lists(TodoToday), e.newPriority).toList)
      }

      case e: TaskMovedToListEvent => {
        for (t <- getTask(e.taskId)) {
          val oldList = e.oldList
          val newList: ListType = e.newList
          lists = lists + (oldList -> lists(oldList).filterNot(_.taskId == t.taskId))
          lists = lists + (e.newList -> (t :: lists(newList)))
          t.list = newList
        }
      }

      case e: PomodoroStartedEvent => withTask(e.taskId, _.apply(e))
      case e: PomodoroEndedEvent => withTask(e.taskId, _.apply(e))
      case e: PomodoroInterruptedEvent => withTask(e.taskId, _.apply(e))
      case e: PomodoroBrokenEvent => withTask(e.taskId, _.apply(e))

      case _ => {}
    }
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
