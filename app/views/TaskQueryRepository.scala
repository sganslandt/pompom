package views

import model.api.{PomodoroStartedEvent, TaskCreatedEvent, UserLoggedInEvent, UserRegisteredEvent}
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.{AfterReplay, BeforeReplay, Replay}


object TaskQueryRepository {

  class Updater extends Actor {

    Akka.system.eventStream.subscribe(self, classOf[UserRegisteredEvent])
    Akka.system.eventStream.subscribe(self, classOf[UserLoggedInEvent])
    Akka.system.eventStream.subscribe(self, classOf[TaskCreatedEvent])
    Akka.system.eventStream.subscribe(self, classOf[PomodoroStartedEvent])

    println("Updater started4")

    def receive = {

      case "init" => {
        println("received init message")
        Akka.system.actorFor("/user/eventStore") ! Replay
      }

      case e: BeforeReplay => {
        println("starting replay")
      }
      case e: AfterReplay => {
        println("replay done")
      }

      case e: UserRegisteredEvent => {
        println("user registered")
        userTasks = userTasks + (e.userId -> List())
      }

      case e: UserLoggedInEvent => {}

      case e: TaskCreatedEvent => {
        println("task created")
        userTasks = userTasks + (e.userId -> (Task(e.taskId, e.title, e.description, List(), false) :: userTasks(e.userId)))
      }

      case event: PomodoroStartedEvent => {}

    }
  }

  def getTask(taskId: String): Option[Task] = None

  private var userTasks: Map[String, List[Task]] = Map()

  def listForUser(userId: String): Seq[Task] = List() //userTasks(userId)
}
