package model

import akka.actor.Actor

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
    }
  }
}

case class Replay()
case class BeforeReplay()
case class AfterReplay()
