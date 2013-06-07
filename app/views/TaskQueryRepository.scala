package views

import model.api._
import akka.actor.{ActorRef, ActorLogging, Actor}
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
                pomodoros: Seq[Pomodoro],
                priority: Int,
                isDone: Boolean) {
  def setPriority(newPriority: Int) = Task(userId, taskId, title, pomodoros, newPriority, isDone)
  def increasePriority() = Task(userId, taskId, title, pomodoros, priority + 1, isDone)
  def decreasePriority() = Task(userId, taskId, title, pomodoros, priority - 1, isDone)
  def startPomodoro(nr: Int) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(Active(now()), pomodoros(nr).interruptions)), priority, isDone)
  def endPomodoro(nr: Int) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(Ended(pomodoros(nr).state.asInstanceOf[Active].startTime, now()), pomodoros(nr).interruptions)), priority, isDone)
  def breakPomodoro(nr: Int, reason: String) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(Broken(pomodoros(nr).state.asInstanceOf[Active].startTime, now(), reason), pomodoros(nr).interruptions)), priority, isDone)
  def interruptPomodoro(nr: Int, reason: String) = Task(userId, taskId, title, pomodoros.updated(nr, Pomodoro(pomodoros(nr).state, Interruption(now(), reason) :: pomodoros(nr).interruptions)), priority, isDone)
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
case class Active(startTime: DateTime) extends PomodoroState
case class Ended(startTime: DateTime, endTime: DateTime) extends PomodoroState
case class Broken(startTime: DateTime, endTime: DateTime, reason: String) extends PomodoroState

class TaskQueryRepository {

  val log = Logging(Akka.system.eventStream, getClass.getName)

  def getTask(taskId: String): Task = {
    val found = tasks.find(_.taskId == taskId)
    found match {
      case None => throw new RuntimeException("Not found")
      case Some(t) => return t
    }
  }

  def listForUser(userId: String): Iterable[Task] = {
    tasks.filter(_.userId == userId).sortBy(_.priority)
  }

  protected def clear() {
    tasks = List()
  }

  protected def createTask(t: Task) {
    tasks = t :: tasks
  }

  protected def replaceTask(newVersion: Task) {
    val index = tasks.indexOf(getTask(newVersion.taskId))
    tasks = tasks.updated(index, newVersion)
  }

  private var tasks: List[Task] = List()

}

object TaskQueryRepository {

  class Updater(eventStore: ActorRef, repo: TaskQueryRepository) extends Actor with ActorLogging {

    Akka.system.eventStream.subscribe(self, classOf[BeforeReplay])
    Akka.system.eventStream.subscribe(self, classOf[AfterReplay])
    Akka.system.eventStream.subscribe(self, classOf[UserRegisteredEvent])
    Akka.system.eventStream.subscribe(self, classOf[UserLoggedInEvent])
    Akka.system.eventStream.subscribe(self, classOf[TaskCreatedEvent])
    Akka.system.eventStream.subscribe(self, classOf[PomodoroStartedEvent])
    Akka.system.eventStream.subscribe(self, classOf[TaskReprioritzedEvent])

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
        eventStore ! Replay
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
      }

      case e: UserLoggedInEvent => {}

      case e: TaskCreatedEvent => {
        log.debug("Task created")
        val currentTaskCount = repo.listForUser(e.userId).size
        repo.createTask(Task(e.userId, e.taskId, e.title, Pomodoro(e.initialEstimate), currentTaskCount, isDone = false))
      }

      case e: TaskReprioritzedEvent => {
        val userTasks = repo.listForUser(e.userId)
        val task = repo.getTask(e.taskId)
        val oldPriority = task.priority
        val newPriority = e.newPriority

        userTasks.foreach(t =>
          if (newPriority < oldPriority && t.priority < oldPriority && t.priority >= newPriority) repo.replaceTask(t.increasePriority)
          else if (newPriority > oldPriority && t.priority > oldPriority && t.priority <= newPriority) repo.replaceTask(t.decreasePriority)
        )

        repo.replaceTask(task.setPriority(e.newPriority))
      }

      case e: PomodoroStartedEvent => {
        repo.replaceTask(repo.getTask(e.taskId).startPomodoro(e.pomodoro))
      }
      case e: PomodoroEndedEvent => {
        repo.replaceTask(repo.getTask(e.taskId).endPomodoro(e.pomodoro))
      }
      case e: PomodoroInterruptedEvent => {
        repo.replaceTask(repo.getTask(e.taskId).interruptPomodoro(e.pomodoro, e.note))
      }
      case e: PomodoroBrokenEvent => {
        repo.replaceTask(repo.getTask(e.taskId).breakPomodoro(e.pomodoro, e.note))
      }

      case _ => {}

    }
  }

}