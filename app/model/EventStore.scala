package model

import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Play.current
import model.api.{DomainEvent, TaskCreatedEvent, UserRegisteredEvent}

class EventStore extends Actor {

  var events: List[DomainEvent] = List(
    TaskCreatedEvent("abc@123", "123", "title", "description", 3),
    UserRegisteredEvent("abc@123")
  )

  def receive = {

    case Replay(Some(aggId)) => {
      events.reverse.filter(_.aggregateIdentifier == aggId).foreach(e => sender ! e)
    }

    case Replay => {
      sender ! BeforeReplay
      events.reverse.foreach(e => sender ! e)
      sender ! AfterReplay
    }

    case e: DomainEvent => {
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
