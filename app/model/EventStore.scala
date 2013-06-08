package model

import akka.actor.{ActorLogging, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.api._
import model.api.TaskCreatedEvent
import model.api.UserRegisteredEvent
import model.api.PomodoroStartedEvent
import scala.Some

class EventStore extends Actor with ActorLogging {

  var events: List[DomainEvent] = List(
    TaskCreatedEvent("abc@123", "6", "postponed stuff 1", 6, 0, ActivityInventory),
    TaskCreatedEvent("abc@123", "5", "postponed stuff 2", 7, 1, ActivityInventory),
    TaskCreatedEvent("abc@123", "4", "postponed stuff 3", 8, 2, ActivityInventory),
    PomodoroBrokenEvent("abc@123", "3", 2, "why this happened?"),
    PomodoroStartedEvent("abc@123", "3", 2),
    PomodoroEndedEvent("abc@123", "3", 1),
    PomodoroInterruptedEvent("abc@123", "3", 1, "like a blitz from klear skies"),
    PomodoroStartedEvent("abc@123", "3", 1),
    PomodoroEndedEvent("abc@123", "3", 0),
    PomodoroStartedEvent("abc@123", "3", 0),
    TaskCreatedEvent("abc@123", "3", "title3", 3, 2, TodoToday),
    PomodoroEndedEvent("abc@123", "2", 1),
    PomodoroStartedEvent("abc@123", "2", 1),
    TaskCreatedEvent("abc@123", "2", "title2", 3, 1, TodoToday),
    PomodoroStartedEvent("abc@123", "1", 0),
    TaskCreatedEvent("abc@123", "1", "title1", 3, 0, TodoToday),
    UserRegisteredEvent("abc@123")
  )

  def receive = {

    case Replay(Some(aggId)) => {
      log.debug("Received Replay({}) message from {}", aggId, sender)
      events.reverse.filter(_.aggregateIdentifier == aggId).foreach(e => sender ! e)
    }

    case Replay => {
      log.debug("Received Replay() message from {}", sender)
      sender ! BeforeReplay
      events.reverse.foreach(e => sender ! e)
      sender ! AfterReplay
    }

    case e: DomainEvent => {
      log.debug("Received DomainEvent message [{}] from {}", e, sender)
      sender ! e
      events = e :: events
      Akka.system.eventStream.publish(e)
    }
  }
}

case class Replay(aggregateIdentifier: Option[String]) {
  def this() = this(None)
}
case class BeforeReplay()
case class AfterReplay()
