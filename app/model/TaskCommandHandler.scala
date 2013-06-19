package model

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import play.api.libs.concurrent.Akka

import play.api.Play.current
import model.api._
import model.api.LoginUserCommand
import java.util.UUID
import org.eligosource.eventsourced.core.{ReplayParams, Eventsourced, EventsourcingExtension, Message}
import play.Logger
import scala.concurrent.stm.Ref
import views.TaskQueryRepository

class TaskCommandHandler(var usersRef: Ref[Map[String, User]], queryRepository: TaskQueryRepository, esExtension: EventsourcingExtension) extends Actor {

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
      self ! Message(users(c.userId).login())
    }

    case c: CreateTaskCommand => self ! Message(users(c.userId).createTask(c.taskId, c.title, c.initialEstimate, c.list))
    case c: ReprioritizeTaskCommand => self ! Message(users(c.userId).reprioritizeTask(c.taskId, c.newPriority))
    case c: MoveTaskToListCommand => {
      val events: Seq[DomainEvent] = users(c.userId).moveTaskToList(c.taskId, c.newList)
      events.foreach(e => self ! Message(e))
    }
    case c: StartPomodoroCommand => self ! Message(users(c.userId).startPomodoro(c.taskId))
    case c: EndPomodoroCommand => self ! Message(users(c.userId).endPomodoro(c.taskId))
    case c: InterruptPomodoroCommand => self ! Message(users(c.userId).interruptPomodoro(c.taskId, c.note))
    case c: BreakPomodoroCommand => self ! Message(users(c.userId).breakPomodoro(c.taskId, c.note))
    case c: CompleteTaskCommand => self ! Message(users(c.userId).completeTask(c.taskId))

    /**
     * Events
     */

    case m: Message => m.event match {
      case e: UserRegisteredEvent => {
        usersRef.single.transform(users => {
          users + (e.userId -> new User(e.userId, e.idProvider, e.id, e.email, e.firstname, e.lastname))
        })
      }

      case e: UserEvent => {
        users(e.userId).apply(m)
      }

      case _ => {}

    }

    Akka.system.eventStream.publish(m) // TODO Use channel instead
  }

}
