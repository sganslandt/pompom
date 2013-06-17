package model

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import play.api.libs.concurrent.Akka

import play.api.Play.current
import model.api._
import model.api.LoginUserCommand
import java.util.UUID
import org.eligosource.eventsourced.core.Message
import play.Logger
import scala.concurrent.stm.Ref
import views.TaskQueryRepository

class TaskCommandHandler(var usersRef: Ref[Map[String, ActorRef]], queryRepository: TaskQueryRepository) extends Actor {

  def users = usersRef.single.get

  def receive = {

    /**
     * Commands
     */

    case c: RegisterUserCommand => {
      self ! Message(UserRegisteredEvent(c.userId, c.idProvider, c.id, c.email, c.firstname, c.lastname))
      self ! Message(UserLoggedInEvent(c.userId))
    }

    case c: LoginUserCommand => {
      users(c.userId) ! c
    }

    case c: CreateTaskCommand => users(c.userId) ! c
    case c: ReprioritizeTaskCommand => users(c.userId) ! c
    case c: MoveTaskToListCommand => users(c.userId) ! c
    case c: StartPomodoroCommand => users(c.userId) ! c
    case c: EndPomodoroCommand => users(c.userId) ! c
    case c: InterruptPomodoroCommand => users(c.userId) ! c
    case c: BreakPomodoroCommand => users(c.userId) ! c
    case c: CompleteTaskCommand => users(c.userId) ! c

    /**
     * Events
     */

    case m: Message => m.event match {
      case e: UserRegisteredEvent => {
        usersRef.single.transform(users => users + (e.userId -> context.actorOf(Props(new User(e.userId)), e.email)))
      }

      case e: UserEvent => users(e.userId) ! e

      case _ => {}

    }

    Akka.system.eventStream.publish(m) // TODO Use channel instead
  }

}
