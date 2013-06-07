package model

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.concurrent.Akka

import play.api.Play.current
import model.api._
import model.api.LoginUserCommand

class TaskCommandHandler(eventStore: ActorRef) extends Actor {

  eventStore ! Replay

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
    case c: ReprioritizeTaskCommand => users(c.userId) ! c
    case c: StartPomodoroCommand => users(c.userId) ! c
    case c: EndPomodoroCommand => users(c.userId) ! c
    case c: InterruptPomodoroCommand => users(c.userId) ! c
    case c: BreakPomodoroCommand => users(c.userId) ! c

    /**
     * Events
     */

    case e: UserRegisteredEvent => {
      users = users + (e.userId -> context.actorOf(Props(new User(e.userId, eventStore)), name = e.userId))
    }

    case _ => {}

  }

  var users: Map[String, ActorRef] = Map()

}
