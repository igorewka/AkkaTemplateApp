package com.is.akkatemplate.actors

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.is.akkatemplate.actors.BoxOffice._

import scala.concurrent.Future
import scala.concurrent.duration._

class BoxOffice extends Actor {

  override def receive: Receive = {

    case In.CreateEvent(eventName, ticketCount) =>
      context.child(eventName) match {
        case Some(_) => sender() ! Out.EventExists
        case None =>
          val ticketSeller = context.actorOf(Props(classOf[TicketSeller], eventName), eventName)
          val tickets = (1 to ticketCount).map(id => TicketSeller.Ticket(id.toString)).toVector

          ticketSeller ! TicketSeller.In.Add(tickets)

          sender() ! Out.EventCreated(Event(eventName, ticketCount))
      }

    case In.BuyTickets(eventName, ticketCount) =>
      context.child(eventName) match {
        case Some(ticketSeller) => ticketSeller.forward(TicketSeller.In.Buy(ticketCount))

        case None => sender() ! Out.EventMissing
      }

    case In.GetEvent(eventName) =>
      context.child(eventName) match {
        case Some(ticketSeller) => ticketSeller.forward(TicketSeller.In.Details)

        case None => sender() ! Out.EventMissing
      }

    case In.GetEvents =>
      implicit val timeout = Timeout(5.seconds)
      implicit val ec = context.system.dispatcher

      val futuresOfEvent = context.children.map(ticketSeller => (ticketSeller ? TicketSeller.In.Details).mapTo[Event])
      Future.sequence(futuresOfEvent).map(events => Out.Events(events.toVector)) pipeTo sender()
  }

}

object BoxOffice {

  case class Event(name: String, ticketCount: Int)

  object In {

    case class CreateEvent(eventName: String, ticketCount: Int)

    case class BuyTickets(eventName: String, ticketCount: Int)

    case class GetEvent(eventName: String)

    case object GetEvents

    case class CancelEvent(eventName: String)

  }

  object Out {

    case object EventExists

    case object EventMissing

    case class EventCreated(event: Event)

    case class Events(events: Vector[Event])

  }

}