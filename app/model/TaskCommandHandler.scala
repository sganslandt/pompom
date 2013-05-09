package model

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.concurrent.Akka
import java.util.UUID

import play.api.Play.current
import model.api._
import model.api.RegisterUserCommand
import model.api.LoginUserCommand
import scala.Some

class TaskCommandHandler extends Actor {

  val eventStore = Akka.system.actorFor("/user/eventStore")

  eventStore ! Replay

  def login(userId: String) {
  }

  def done() {}

  def extendEstimate(i: Int) {}

  def break(s: String) {}

  def interrupt(s: String) {}

  def endPomodoro() {}


  def startPomodoro(s: String) {}

  var tasks: Map[String, List[ActorRef]] = Map()

  var users: Map[String, ActorRef] = Map()

  def receive = {

    /**
     * Commands
     */

    case c: LoginUserCommand => {
      val userId = c.userId
      if (!users.contains(userId)) eventStore ! UserRegisteredEvent(userId)
      else users(userId) ! LoginUserCommand(userId)
    }

    case c: CreateTaskCommand => users(c.userId) ! c

    /**
     * Events
     */

    case e: UserRegisteredEvent => {
      users = users + (e.userId -> context.actorOf(Props(new User(e.userId)), name = e.userId))
    }

  }
}
