package model

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.concurrent.Akka
import java.util.UUID

import play.api.Play.current
import model.api.{TaskCreatedEvent, TaskEvent}

class TaskCommandHandler(eventStore: EventStore) extends Actor {

  eventStore.replay(this)

  def createTask(userId: User, title: String, initialEstimate: Int, description: String): String = {
    val id = UUID.randomUUID().toString
    val userTasks = tasks.get(userId).getOrElse(EMPTY)
    val newTask = Akka.system.actorOf(Props(new Task(id, title, description, initialEstimate)))
    val newEntry: (User, List[ActorRef]) = (userId, newTask :: userTasks)
    tasks = tasks + newEntry

    id
  }

  def done() {}

  def extendEstimate(i: Int) {}

  def break(s: String) {}

  def interrupt(s: String) {}

  def endPomodoro() {}


  def startPomodoro(s: String) {}

  type User = String

  val EMPTY: List[ActorRef] = List(Akka.system.actorOf(Props(new Task("id", "title", "desc", 3))))

  var tasks: Map[User, List[ActorRef]] = Map()

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
    case e: TaskEvent => {
      eventStore ! e
      getOrCreateTask(e.taskId) ! e
    }
  }

  def getOrCreateTask(taskId: String) = {
    Akka.system.actorOf(Props[Task])
  }
}
