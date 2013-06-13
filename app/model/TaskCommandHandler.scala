package model

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.concurrent.Akka

import play.api.Play.current
import model.api._
import model.api.LoginUserCommand
import java.util.UUID

class TaskCommandHandler(eventStore: ActorRef) extends Actor {

  eventStore ! Replay

  def receive = {

    /**
     * Commands
     */

    case c: RegisterUserCommand => {
      if (!users.contains(c.email)) {
        val userId: String = UUID.randomUUID().toString
        eventStore ! UserRegisteredEvent(userId, c.idProvider, c.id, c.email, c.firstname, c.lastname)
      }
    }

    case c: LoginUserCommand => {
      val email = c.email
      users(email) ! c
    }

    case c: CreateTaskCommand => users(c.userId) ! c
    case c: ReprioritizeTaskCommand => users(c.userId) ! c
    case c: MoveTaskToListCommand => users(c.userId) ! c
    case c: StartPomodoroCommand => users(c.userId) ! c
    case c: EndPomodoroCommand => users(c.userId) ! c
    case c: InterruptPomodoroCommand => users(c.userId) ! c
    case c: BreakPomodoroCommand => users(c.userId) ! c

    /**
     * Events
     */

    case e: DomainEventMessage => e.payload match {
      case e: UserRegisteredEvent => {
        users = users + (e.email -> context.actorOf(Props(new User(e.userId, eventStore)), name = e.email))
      }

      case _ => {}
    }
  }

  var users: Map[String, ActorRef] = Map()

}
