package views

import model.api._
import akka.actor.{ActorLogging, Actor}
import akka.event.Logging
import play.api.libs.concurrent.Akka
import play.api.Play.current

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import model.api.UserRegisteredEvent
import model.Replay
import model.api.UserLoggedInEvent
import model.api.PomodoroStartedEvent
import scala.Some
import model.BeforeReplay
import model.AfterReplay
import model.api.TaskCreatedEvent

case class Task(userId: String,
                taskId: String,
                title: String,
                description: String,
                pomodoros: Seq[Pomodoro],
                isDone: Boolean) {
  def startPomodoro(nr: Int) = Task(userId, taskId, title, description, pomodoros.updated(nr, Pomodoro(InProgress(now()), pomodoros(nr).interruptions)), false)
  def endPomodoro(nr: Int) = Task(userId, taskId, title, description, pomodoros.updated(nr, Pomodoro(Ended(pomodoros(nr).state.asInstanceOf[InProgress].startTime, now()), pomodoros(nr).interruptions)), false)
  def breakPomodoro(nr: Int, reason: String) = Task(userId, taskId, title, description, pomodoros.updated(nr, Pomodoro(Broken(pomodoros(nr).state.asInstanceOf[InProgress].startTime, now(), reason), pomodoros(nr).interruptions)), false)
  def interruptPomodoro(nr: Int, reason: String) = Task(userId, taskId, title, description, pomodoros.updated(nr, Pomodoro(pomodoros(nr).state, Interruption(now(), reason) :: pomodoros(nr).interruptions)), false)
}

case class Pomodoro(state: PomodoroState, interruptions: List[Interruption])

case object Pomodoro {
  def apply(): Pomodoro = {
    new Pomodoro(Fresh, List())
  }

  def apply(nrOfPomodoros: Int): List[Pomodoro] = nrOfPomodoros match {
    case 0 => List()
    case n => apply() :: apply(n - 1)
  }
}

case class Interruption(when: DateTime, what: String) {}

trait PomodoroState
case object Fresh extends PomodoroState
case class InProgress(startTime: DateTime) extends PomodoroState
case class Ended(startTime: DateTime, endTime: DateTime) extends PomodoroState
case class Broken(startTime: DateTime, endTime: DateTime, reason: String) extends PomodoroState

class TaskQueryRepository {

  val log = Logging(Akka.system.eventStream, getClass.getName)

  def getTask(taskId: String): Option[Task] = {
    userTasks.values.find(_.contains(taskId)).flatMap(m => Some(m(taskId)))
  }

  def listForUser(userId: String): Iterable[Task] = {
    if (userTasks.contains(userId)) {
      log.debug("Listing tasks for userId {}, [{}]", userId, userTasks(userId))
      userTasks(userId).map(_._2)
    }
    else {
      log.debug("No tasks found for user {}, returning empty list.", userId)
      List() // TODO Make this one not needed
    }
  }

  protected def clear() {
    userTasks = Map()
  }

  protected def addUser(userId: String) {
    userTasks = userTasks + (userId -> Map())
  }

  protected def createOrReplaceTask(userId: String, t: Task) {
    userTasks = userTasks + (userId -> (userTasks(userId) + (t.taskId -> t)))
  }

  private var userTasks: Map[String, Map[String, Task]] = Map()

}

object TaskQueryRepository {

  class Updater(repo: TaskQueryRepository) extends Actor with ActorLogging {

    Akka.system.eventStream.subscribe(self, classOf[BeforeReplay])
    Akka.system.eventStream.subscribe(self, classOf[AfterReplay])
    Akka.system.eventStream.subscribe(self, classOf[UserRegisteredEvent])
    Akka.system.eventStream.subscribe(self, classOf[UserLoggedInEvent])
    Akka.system.eventStream.subscribe(self, classOf[TaskCreatedEvent])
    Akka.system.eventStream.subscribe(self, classOf[PomodoroStartedEvent])

    override def preStart() = {
      log.debug("Starting")
    }

    override def preRestart(reason: Throwable, message: Option[Any]) {
      log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
    }

    def receive = {

      case "init" => {
        log.debug("Received init message")
        Akka.system.actorFor("/user/eventStore") ! Replay
      }

      case e: BeforeReplay => {
        log.debug("Starting replay")
        repo.clear()
      }
      case e: AfterReplay => {
        log.debug("Replay done")
      }

      case e: UserRegisteredEvent => {
        log.debug("User registered")
        repo.addUser(e.userId)
      }

      case e: UserLoggedInEvent => {}

      case e: TaskCreatedEvent => {
        log.debug("Task created")
        repo.createOrReplaceTask(e.userId, Task(e.userId, e.taskId, e.title, e.description, Pomodoro(e.initialEstimate), false))
      }

      case e: PomodoroStartedEvent => {
        repo.getTask(e.taskId) match {
          case Some(t) => repo.createOrReplaceTask(t.userId, t.startPomodoro(e.pomodoro))
        }
      }
      case e: PomodoroEndedEvent => {
        repo.getTask(e.taskId) match {
          case Some(t) => repo.createOrReplaceTask(t.userId, t.endPomodoro(e.pomodoro))
        }
      }
      case e: PomodoroInterruptedEvent => {
        repo.getTask(e.taskId) match {
          case Some(t) => repo.createOrReplaceTask(t.userId, t.interruptPomodoro(e.pomodoro, e.note))
        }
      }
      case e: PomodoroBrokenEvent => {
        repo.getTask(e.taskId) match {
          case Some(t) => repo.createOrReplaceTask(t.userId, t.breakPomodoro(e.pomodoro, e.note))
        }
      }

      case _ => {}

    }
  }

}