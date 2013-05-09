package model

import akka.actor.{ActorRef, Actor}
import model.api._
import model.api.UserLoggedInEvent
import model.api.LoginUserCommand
import play.api.libs.concurrent.Akka
import play.api.Play.current

class User(userId: String) extends Actor {

  private var prioritizedTaskList: List[ActorRef] = List()
  private val settings: Settings = new Settings()

  val eventStore = Akka.system.actorFor("/user/eventStore")
  eventStore ! Replay(Some(userId))

  def receive = {
    case _: LoginUserCommand => eventStore ! UserLoggedInEvent(userId)
    case c: CreateTaskCommand => eventStore ! TaskCreatedEvent(c.userId, c.taskId, c.title, c.description, c.initialEstimate)

    case e: TaskCreatedEvent => {
      prioritizedTaskList = sender :: prioritizedTaskList
    }

    case _ => {}
  }
}

class Settings {
  private var pomodoroLength: Int = 25
}
