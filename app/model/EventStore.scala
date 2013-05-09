package model

import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Play.current

class EventStore extends Actor {

  var events: List[AnyRef] = List()

  def receive = {

    case e: Replay => {
      sender ! BeforeReplay
      events.reverse.foreach(e => sender ! e)
      sender ! AfterReplay
    }

    case e: AnyRef => {
      events = e :: events
      Akka.system.eventStream.publish(e)
    }
  }
}

case class Replay()
case class BeforeReplay()
case class AfterReplay()
