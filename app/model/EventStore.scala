package model

import akka.actor.{ActorLogging, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.api._
import model.api.TaskCreatedEvent
import model.api.UserRegisteredEvent
import model.api.PomodoroStartedEvent
import scala.Some
import org.joda.time.DateTime
import org.joda.time.DateTime.now
import java.util.UUID

class EventStore extends Actor with ActorLogging {

  override def preStart() {
    log.debug("Starting")
  }

  var events: List[DomainEventMessage] = List(
    DomainEventMessage("abc@123", 0, now, TaskCreatedEvent("abc@123", "6", "postponed stuff 1", 6, 0, ActivityInventory)),
    DomainEventMessage("abc@123", 0, now, TaskCreatedEvent("abc@123", "5", "postponed stuff 2", 7, 1, ActivityInventory)),
    DomainEventMessage("abc@123", 0, now, TaskCreatedEvent("abc@123", "4", "postponed stuff 3", 8, 2, ActivityInventory)),
    DomainEventMessage("abc@123", 0, now, PomodoroBrokenEvent("abc@123", "3", 2, "why this happened?")),
    DomainEventMessage("abc@123", 0, now, PomodoroStartedEvent("abc@123", "3", 2)),
    DomainEventMessage("abc@123", 0, now, PomodoroEndedEvent("abc@123", "3", 1)),
    DomainEventMessage("abc@123", 0, now, PomodoroInterruptedEvent("abc@123", "3", 1, "like a blitz from klear skies")),
    DomainEventMessage("abc@123", 0, now, PomodoroStartedEvent("abc@123", "3", 1)),
    DomainEventMessage("abc@123", 0, now, PomodoroEndedEvent("abc@123", "3", 0)),
    DomainEventMessage("abc@123", 0, now, PomodoroStartedEvent("abc@123", "3", 0)),
    DomainEventMessage("abc@123", 0, now, TaskCreatedEvent("abc@123", "3", "title3", 3, 2, TodoToday)),
    DomainEventMessage("abc@123", 0, now, PomodoroEndedEvent("abc@123", "2", 1)),
    DomainEventMessage("abc@123", 0, now, PomodoroStartedEvent("abc@123", "2", 1)),
    DomainEventMessage("abc@123", 0, now, TaskCreatedEvent("abc@123", "2", "title2", 3, 1, TodoToday)),
    DomainEventMessage("abc@123", 0, now, PomodoroStartedEvent("abc@123", "1", 0)),
    DomainEventMessage("abc@123", 0, now, TaskCreatedEvent("abc@123", "1", "title1", 3, 0, TodoToday)),
    DomainEventMessage("abc@123", 0, now, UserRegisteredEvent(UUID.randomUUID().toString, "local", UUID.randomUUID().toString, "abc@123", "firstname", "lastname"))
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

      val eventMessage: DomainEventMessage = DomainEventMessage(e.aggregateIdentifier, 0, now, e)
      events = eventMessage :: events

      sender ! eventMessage
      Akka.system.eventStream.publish(eventMessage)
    }
  }
}

case class DomainEventMessage(aggregateIdentifier: String, sequenceNumber: Long, timestamp: DateTime, payload: DomainEvent)

case class Replay(aggregateIdentifier: Option[String]) {
  def this() = this(None)
}
case class BeforeReplay()
case class AfterReplay()
