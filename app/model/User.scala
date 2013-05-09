package model

import akka.actor.{Props, ActorRef, Actor}
import model.api._
import model.api.UserLoggedInEvent
import model.api.RegisterUserCommand
import model.api.UserRegisteredEvent
import model.api.LoginUserCommand

class User(userId: String) extends Actor {

  private var prioritizedTaskList: List[ActorRef] = List()
  private val settings: Settings = new Settings()

  def receive = {
    case _: RegisterUserCommand => self ! UserRegisteredEvent(userId)
    case _: LoginUserCommand => self ! UserLoggedInEvent(userId)
    case c: CreateTaskCommand => {
      context.actorOf(Props(new Task(c.taskId, c.title, c.description, c.initialEstimate)), name = c.taskId) ! c
    }

    case e: UserLoggedInEvent => {
      context.parent ! e
    }
    case e: TaskCreatedEvent => {
      prioritizedTaskList = sender :: prioritizedTaskList
      context.parent ! e
    }

    case e => {
      context.parent ! e
    }
  }
}

class Settings {
  private var pomodoroLength: Int = 25
}
