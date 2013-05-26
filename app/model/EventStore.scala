package model

import akka.actor.{ActorLogging, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.api.{DomainEvent, TaskCreatedEvent, UserRegisteredEvent}

class EventStore extends Actor with ActorLogging {

  var events: List[DomainEvent] = List(
    TaskCreatedEvent("abc@123", "123", "title", "description", 3),
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
