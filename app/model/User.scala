package model

import model.api._
import akka.actor.Cancellable
import model.api.TaskCompletedEvent
import model.api.TaskCreatedEvent
import model.api.PomodoroInterruptedEvent
import model.api.UserRegisteredEvent
import model.api.TaskMovedToListEvent
import model.api.UserLoggedInEvent
import org.eligosource.eventsourced.core.Message
import model.api.TaskReprioritzedEvent
import model.api.PomodoroBrokenEvent
import scala.Some
import com.github.nscala_time.time.StaticForwarderImports.Duration
import com.github.nscala_time.time.TypeImports.Duration

class User(userId: String, idProvider: String, id: String, email: String, firstname: String, lastname: String) {

  private var tasks: List[Task] = List()
  private var endSchedules: Map[String, Cancellable] = Map()

  private val settings: Settings = new Settings()

  new UserRegisteredEvent(userId, idProvider, id, email, firstname, lastname)

  def login() = {
    UserLoggedInEvent(userId)
  }

  def createTask(taskId: String, title: String, initialEstimate: Int, list: ListType) = {
    val priority = tasks.count(_.list == list)
    TaskCreatedEvent(userId, taskId, title, initialEstimate, priority, list)
  }

  def reprioritizeTask(taskId: String, newPriority: Int) = {
    TaskReprioritzedEvent(userId, taskId, newPriority)
  }

  def moveTaskToList(taskId: String, newList: ListType): Seq[DomainEvent] = {
    getTask(taskId) match {
      case Some(task) => {
        List(
          TaskMovedToListEvent(userId, taskId, task.list, newList),
          TaskReprioritzedEvent(userId, taskId, tasks.count(_.list == newList))
        )
      }
      case None => {
        List()
      }
    }}

  def startPomodoro(taskId: String) = withTask(taskId, _.startPomodoro()).get
  def endPomodoro(taskId: String) = withTask(taskId, _.endPomodoro()).get
  def interruptPomodoro(taskId: String, note: String)     = withTask(taskId, _.interrupt(note)).get
  def breakPomodoro(taskId: String, note: String)     = withTask(taskId, _.break(note)).get
  def completeTask(taskId: String) = TaskCompletedEvent(userId, taskId)

  def apply(eventMessage: Message) {
    eventMessage.event match {
      case e: TaskCreatedEvent => tasks = new Task(userId, e.taskId, e.title, e.initialEstimate, e.list) :: tasks
      case e: TaskMovedToListEvent => {
        withTask(e.taskId, _.setList(e.newList))
      }

      //      case e: PomodoroStartedEvent => {
      //        withTask(e.taskId, _.apply(e))
      //        val startTime = new DateTime(eventMessage.timestamp)
      //        val endTime = startTime + settings.pomodoroLength
      //        val now = DateTime.now
      //        val timeLeft = now to endTime
      //        val endPomodoroSchedule: Cancellable = Akka.system.scheduler.scheduleOnce(FiniteDuration(timeLeft.toDurationMillis, TimeUnit.MILLISECONDS), self, EndPomodoroCommand(userId))
      //        endSchedules = endSchedules + (e.taskId -> endPomodoroSchedule)
      //      }
      //      case e: PomodoroEndedEvent => {
      //        endSchedules(e.taskId).cancel()
      //        withTask(e.taskId, _.apply(e))
      //      }
      case e: PomodoroInterruptedEvent => withTask(e.taskId, _.apply(e))
      case e: PomodoroBrokenEvent => withTask(e.taskId, _.apply(e))
      case e: TaskCompletedEvent => withTask(e.taskId, _.apply(e))

      case _ => {}
    }
  }

  private def getTask(taskId: String): Option[Task] = {
    tasks.find(_.taskId == taskId)
  }

  private def withTask[T](taskId: String, f: Task => T): Option[T] = {
    for (t <- tasks.find(_.taskId == taskId)) yield f(t)
  }

}

class Settings {
  val pomodoroLength: Duration = Duration.standardSeconds(10)
}
