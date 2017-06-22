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
        case Some(_) =>
          sender() ! Out.EventExists
        case None =>
          val ticketSeller = context.actorOf(Props(classOf[TicketSeller], eventName), eventName)
          val tickets = (1 to ticketCount).map(id => TicketSeller.Ticket(id.toString)).toVector

          ticketSeller ! TicketSeller.In.Add(tickets)

          sender() ! Out.EventCreated(Out.Event(eventName, ticketCount))
      }

    case In.BuyTickets(eventName, ticketCount) =>
      context.child(eventName) match {
        case Some(ticketSeller) =>
          ticketSeller.forward(TicketSeller.In.Buy(ticketCount))
        case None =>
          sender() ! Out.EventMissing
      }

    case In.GetEvent(eventName) =>
      context.child(eventName) match {
        case Some(ticketSeller) =>
          ticketSeller.forward(TicketSeller.In.Details)
        case None =>
          sender() ! Out.EventMissing
      }

    case In.GetEvents =>
      implicit val timeout = Timeout(5.seconds)
      implicit val ec = context.system.dispatcher

      val futuresOfEvent = context.children.map(ticketSeller => (ticketSeller ? TicketSeller.In.Details).mapTo[Out.Event])
      Future.sequence(futuresOfEvent).map(events => Out.Events(events.toVector)) pipeTo sender()

    case In.CancelEvent(eventName) =>
      context.child(eventName) match {
        case Some(ticketSeller) =>
          ticketSeller.forward(TicketSeller.In.Cancel)
        case None =>
          sender() ! Out.EventMissing
      }
  }

}

object BoxOffice {

  val name = "boxoffice"

  object In {

    case class CreateEvent(eventName: String, ticketCount: Int)

    case class BuyTickets(eventName: String, ticketCount: Int)

    case class GetEvent(eventName: String)

    case object GetEvents

    case class CancelEvent(eventName: String)

  }

  object Out {

    case class Event(name: String, ticketCount: Int) extends RestApi.Response

    case object EventExists extends RestApi.Response

    case object EventMissing extends RestApi.Response

    case class EventCreated(event: Event) extends RestApi.Response

    case class Events(events: Vector[Event]) extends RestApi.Response

  }

}