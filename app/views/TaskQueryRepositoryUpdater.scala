package views

import model.api.{PomodoroStartedEvent, TaskCreatedEvent}
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Play.current

class TaskQueryRepositoryUpdater extends Actor {

  Akka.system.eventStream.subscribe(self, classOf[TaskCreatedEvent])
  Akka.system.eventStream.subscribe(self, classOf[PomodoroStartedEvent])

  println("TaskQueryRepoUpdater started.")

  def receive = {

    case event: TaskCreatedEvent => {
      println("New task created!")
    }

    case event: PomodoroStartedEvent => {}

  }

}
