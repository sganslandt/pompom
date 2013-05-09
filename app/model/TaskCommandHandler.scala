package model

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.concurrent.Akka
import java.util.UUID

import play.api.Play.current
import model.api._
import model.Replay
import model.api.RegisterUserCommand
import model.api.LoginUserCommand
import scala.Some

class TaskCommandHandler(val eventStore: ActorRef) extends Actor {

  eventStore ! Replay()

  def login(userId: String) {
  }

  def createTask(userId: String, title: String, initialEstimate: Int, description: String): String = {
    val id = UUID.randomUUID().toString
    val userTasks = tasks.get(userId).getOrElse(EMPTY)
    val newTask = Akka.system.actorOf(Props(new Task(id, title, description, initialEstimate)))
    tasks = tasks + (userId -> (newTask :: userTasks))

    id
  }

  def done() {}

  def extendEstimate(i: Int) {}

  def break(s: String) {}

  def interrupt(s: String) {}

  def endPomodoro() {}


  def startPomodoro(s: String) {}

  val EMPTY: List[ActorRef] = List(Akka.system.actorOf(Props(new Task("id", "title", "desc", 3))))

  var tasks: Map[String, List[ActorRef]] = Map()

  var users: Map[String, ActorRef] = Map()

  def getTask(userId: User, taskId: String): Option[ActorRef] = {
    Some(Akka.system.actorOf(Props[Task]))
    //    val tasksForUser = tasks.get(userId).getOrElse(EMPTY)
    //    tasksForUser.find({
    //      task => task.taskId == taskId
    //    })
  }

  def listForUser(userId: String): Seq[ActorRef] = {
    tasks.get(userId).getOrElse(EMPTY)
  }


  def receive = {

    /**
     * Commands
     */

    case c: LoginUserCommand => {
      val userId = c.userId
      if (!users.contains(userId)) {
        users = users + (userId -> context.actorOf(Props(new User(userId))))
        users(userId) ! RegisterUserCommand()
      }

      users(userId) ! LoginUserCommand(userId)
    }

    case c: CreateTaskCommand => users(c.userId) ! c

    /**
     * Events
     */

    case e: UserEvent => {
      eventStore ! e
    }

    case e: TaskEvent => {
      eventStore ! e
    }
  }
}
