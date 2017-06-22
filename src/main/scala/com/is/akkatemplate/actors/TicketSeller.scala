package com.is.akkatemplate.actors

import akka.actor.{Actor, PoisonPill}
import com.is.akkatemplate.actors.TicketSeller._

class TicketSeller(eventName: String) extends Actor {

  private var tickets = Vector[Ticket]()

  override def receive: Receive = {

    case In.Add(newTickets) =>
      tickets = tickets ++ newTickets

    case In.Buy(ticketCount) =>
      if (tickets.size >= ticketCount) {
        val ticketsSold = tickets.take(ticketCount)
        tickets = tickets.drop(ticketCount)
        sender() ! Out.Tickets(eventName, ticketsSold)
      } else {
        sender() ! Out.Tickets(eventName)
      }

    case In.Details =>
      sender() ! BoxOffice.Out.Event(eventName, tickets.size)

    case In.Cancel =>
      sender() ! BoxOffice.Out.Event(eventName, tickets.size)
      self ! PoisonPill
  }

}

object TicketSeller {

  case class Ticket(id: String)

  object In {

    case class Add(tickets: Vector[Ticket])

    case class Buy(ticketCount: Int)

    case object Details

    case object Cancel

  }

  object Out {

    case class Tickets(eventName: String, tickets: Vector[Ticket] = Vector[Ticket]()) extends RestApi.Response

  }

}
