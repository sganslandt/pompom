package views

import model.api.{PomodoroStartedEvent, TaskCreatedEvent, UserLoggedInEvent, UserRegisteredEvent}
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.{AfterReplay, BeforeReplay, Replay}


class TaskQueryRepository {

  def clear() {
    userTasks = Map()
  }

  def addUser(userId: String) {
    userTasks = userTasks + (userId -> List())
  }

  def addTask(userId: String, t: Task) {
    userTasks = userTasks + (userId -> (Task(t.taskId, t.title, t.description, List(), false) :: userTasks(userId)))
  }

  private var userTasks: Map[String, List[Task]] = Map()

  def listForUser(userId: String): Seq[Task] = {
    if (userTasks.contains(userId))
      userTasks(userId)
    else
      List() // TODO Make this one not needed
  }
}

object TaskQueryRepository {

  class Updater(repo: TaskQueryRepository) extends Actor {

    Akka.system.eventStream.subscribe(self, classOf[BeforeReplay])
    Akka.system.eventStream.subscribe(self, classOf[AfterReplay])
    Akka.system.eventStream.subscribe(self, classOf[UserRegisteredEvent])
    Akka.system.eventStream.subscribe(self, classOf[UserLoggedInEvent])
    Akka.system.eventStream.subscribe(self, classOf[TaskCreatedEvent])
    Akka.system.eventStream.subscribe(self, classOf[PomodoroStartedEvent])

    println("Updater started")

    def receive = {

      case "init" => {
        println("received init message")
        Akka.system.actorFor("/user/eventStore") ! Replay
      }

      case e: BeforeReplay => {
        println("starting replay")
        repo.clear()
      }
      case e: AfterReplay => {
        println("replay done")
      }

      case e: UserRegisteredEvent => {
        println("user registered")
        repo.addUser(e.userId)
      }

      case e: UserLoggedInEvent => {}

      case e: TaskCreatedEvent => {
        println("task created")
        repo.addTask(e.userId, Task(e.taskId, e.title, e.description, List(), false))
      }

      case event: PomodoroStartedEvent => {}

    }
  }

}
