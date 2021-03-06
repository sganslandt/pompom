package views

import model.api._
import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.event.Logging
import play.api.libs.concurrent.Akka
import play.api.Play.current

import org.joda.time.DateTime
import model.api.UserRegisteredEvent
import model.{DomainEventMessage, Replay, BeforeReplay, AfterReplay}
import model.api.UserLoggedInEvent
import model.api.PomodoroStartedEvent
import scala.Some
import model.api.TaskCreatedEvent
import util.Prioritizable
import util.Prioritizable.rePrioritize
import org.eligosource.eventsourced.core.Message

case class User(userId: String,
                 email: String)

case class Task(userId: String,
                taskId: String,
                title: String,
                pomodoros: Seq[Pomodoro],
                priority: Int,
                createdTime: DateTime,
                doneTime: Option[DateTime],
                list: ListType) extends Prioritizable[Task] {
  override def setPriority(newPriority: Int) = Task(userId, taskId, title, pomodoros, newPriority, createdTime, doneTime, list)
  override def increasePriority() = Task(userId, taskId, title, pomodoros, priority + 1, createdTime, doneTime, list)
  override def decreasePriority() = Task(userId, taskId, title, pomodoros, priority - 1, createdTime, doneTime, list)
  def startPomodoro(nr: Int, timestamp: DateTime) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(Active(timestamp), pomodoros(nr).interruptions)), priority, createdTime, doneTime, list)
  def endPomodoro(nr: Int, timestamp: DateTime) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(Finished(pomodoros(nr).state.asInstanceOf[Active].startTime, timestamp), pomodoros(nr).interruptions)), priority, createdTime, doneTime, list)
  def breakPomodoro(nr: Int, reason: String, timestamp: DateTime) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(Broken(pomodoros(nr).state.asInstanceOf[Active].startTime, timestamp, reason), pomodoros(nr).interruptions)), priority, createdTime, doneTime, list)
  def interruptPomodoro(nr: Int, reason: String, timestamp: DateTime) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(pomodoros(nr).state, Interruption(timestamp, reason) :: pomodoros(nr).interruptions)), priority, createdTime, doneTime, list)
  def movedToList(newList: ListType) = Task(userId, taskId, title, pomodoros, priority,  createdTime, doneTime, newList)
  def done(timestamp: DateTime) = Task(userId, taskId, title, pomodoros, priority,  createdTime, Some(timestamp), list)
}

case class Pomodoro(state: PomodoroState, interruptions: List[Interruption])

case object Pomodoro {
  def apply(): Pomodoro = {
    new Pomodoro(Fresh(), List())
  }

  def apply(nrOfPomodoros: Int): List[Pomodoro] = nrOfPomodoros match {
    case 0 => List()
    case n => apply() :: apply(n - 1)
  }
}

case class Interruption(when: DateTime, what: String) {}

trait PomodoroState
trait Started {
  def startTime: DateTime
}
trait Ended extends Started {
  def endTime: DateTime
}

case class Fresh() extends PomodoroState
case class Active(override val startTime: DateTime) extends PomodoroState with Started
case class Finished(override val startTime: DateTime, override val endTime: DateTime) extends PomodoroState with Ended
case class Broken(override val startTime: DateTime, override val endTime: DateTime, reason: String) extends PomodoroState with Ended

class TaskQueryRepository {

  val log = Logging(Akka.system.eventStream, getClass.getName)

  def getUserByEmail(email: String): Option[User] = {
    if (users.filter( _.email == email).size == 0) None
    else Some(users.filter( _.email == email)(0))
  }

  def isUserRegistered(userId: String) = users.filter( _.userId == userId).size != 0

  def getTask(taskId: String): Task = {
    val found = tasks.find(_.taskId == taskId)
    found match {
      case None => throw new RuntimeException("Not found")
      case Some(t) => return t
    }
  }

  def listTodoToday(userId: String): Iterable[Task] = {
    tasks.filter(_.userId == userId).filter(_.list == TodoToday).sortBy(_.priority)
  }

  def listActivityInventory(userId: String): Iterable[Task] = {
    tasks.filter(_.userId == userId).filter(_.list == ActivityInventory).sortBy(_.priority)
  }

  protected def clear() {
    users = List()
    tasks = List()
  }

  protected def addUser(u: User) {
    users = u :: users
  }

  protected def createTask(t: Task) {
    tasks = t :: tasks
  }

  protected def replaceTask(newVersion: Task) {
    val index = tasks.indexOf(getTask(newVersion.taskId))
    tasks = tasks.updated(index, newVersion)
  }

  private var users: List[User] = List()
  private var tasks: List[Task] = List()

}

object TaskQueryRepository {

  class Updater(repo: TaskQueryRepository) extends Actor with ActorLogging {

    Akka.system.eventStream.subscribe(self, classOf[Message])

    override def preStart() {
      log.debug("Starting")
    }

    override def preRestart(reason: Throwable, message: Option[Any]) {
      log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
    }

    def receive = {

      case "init" => {
        log.debug("Received init message")
      }

      case m: Message => {

        log.info("Received {}", m)

        m.event match {

        case e: UserRegisteredEvent => {
          repo.addUser(User(e.userId, e.email))
        }

        case e: UserLoggedInEvent => {}

        case e: TaskCreatedEvent => {
          repo.createTask(Task(e.userId, e.taskId, e.title, Pomodoro(e.initialEstimate), e.priority, new DateTime(m.timestamp), None, e.list))
        }

        case e: TaskReprioritzedEvent => {
          val task = repo.getTask(e.taskId)
          val userTasks = task.list match {
            case TodoToday => repo.listTodoToday(e.userId)
            case ActivityInventory => repo.listActivityInventory(e.userId)
          }
          val newPriority = e.newPriority

          val reprioritizedTasks: Iterable[Prioritizable[Task]] = rePrioritize(task, userTasks, newPriority)
          for (t <- reprioritizedTasks) repo.replaceTask(t.asInstanceOf[Task])
        }

        case e: TaskMovedToListEvent => {
          val currentTask: Task = repo.getTask(e.taskId)
          repo.replaceTask(currentTask.movedToList(e.newList))
        }

        case e: PomodoroStartedEvent => {
          repo.replaceTask(repo.getTask(e.taskId).startPomodoro(e.pomodoro, new DateTime(m.timestamp)))
        }
        case e: PomodoroEndedEvent => {
          repo.replaceTask(repo.getTask(e.taskId).endPomodoro(e.pomodoro, new DateTime(m.timestamp)))
        }
        case e: PomodoroInterruptedEvent => {
          repo.replaceTask(repo.getTask(e.taskId).interruptPomodoro(e.pomodoro, e.note, new DateTime(m.timestamp)))
        }
        case e: PomodoroBrokenEvent => {
          repo.replaceTask(repo.getTask(e.taskId).breakPomodoro(e.pomodoro, e.note, new DateTime(m.timestamp)))
        }
        case e: TaskCompletedEvent => {
          repo.replaceTask(repo.getTask(e.taskId).done(new DateTime(m.timestamp)))
        }

        case _ => {}

      }                 }
    }
  }

}