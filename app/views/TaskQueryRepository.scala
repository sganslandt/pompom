package views

import model.api.{PomodoroStartedEvent, TaskCreatedEvent, UserLoggedInEvent, UserRegisteredEvent}
import akka.actor.{ActorLogging, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.{AfterReplay, BeforeReplay, Replay}
import akka.event.Logging
import org.joda.time.DateTime

case class Task(taskId: String,
                title: String,
                description: String,
                pomodoros: Seq[Pomodoro],
                isDone: Boolean) {
}

case class Pomodoro(state: PomodoroState, interruptions: Seq[Interruption])
case object Pomodoro {
  def apply(): Pomodoro = { new Pomodoro(Fresh, List()) }
  def apply(nrOfPomodoros: Int): List[Pomodoro] = nrOfPomodoros match {
    case 0 => List()
    case n => apply() :: apply(n - 1)
  }
}

case class Interruption(when: DateTime, what: String) {}

trait PomodoroState
case object Fresh extends PomodoroState
case class Active(startTime: DateTime) extends PomodoroState
case class Ended(startTime: DateTime, endTime: DateTime) extends PomodoroState
case class Broken(startTime: DateTime, endTime: DateTime, reason: String) extends PomodoroState

class TaskQueryRepository {

  val log = Logging(Akka.system.eventStream, getClass.getName)

  def clear() {
    userTasks = Map()
  }

  def addUser(userId: String) {
    userTasks = userTasks + (userId -> List())
  }

  def addTask(userId: String, t: Task) {
    userTasks = userTasks + (userId -> (t :: userTasks(userId)))
  }

  private var userTasks: Map[String, List[Task]] = Map()

  def listForUser(userId: String): Seq[Task] = {
    if (userTasks.contains(userId)) {
      log.debug("Listing tasks for userId {}, [{}]", userId, userTasks(userId))
      userTasks(userId)
    }
    else {
      log.debug("No tasks found for user {}, returning empty list.", userId)
      List() // TODO Make this one not needed
    }
  }
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
        repo.addTask(e.userId, Task(e.taskId, e.title, e.description, Pomodoro(e.initialEstimate), false))
      }

      case event: PomodoroStartedEvent => {}

    }
  }

}