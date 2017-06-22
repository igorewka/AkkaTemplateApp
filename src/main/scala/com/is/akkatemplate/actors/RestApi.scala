package com.is.akkatemplate.actors

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatchers, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.is.akkatemplate.actors.RestApi.{Error, EventDescription, TicketRequest}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class RestApi(system: ActorSystem) {

  private lazy val boxOffice = system.actorOf(Props[BoxOffice])

  implicit val timeout = Timeout(5.seconds)
  implicit val ec: ExecutionContext = system.dispatcher

  def route(): Route = {
    pathPrefix("events" / PathMatchers.Segment) { eventName =>
      pathEndOrSingleSlash {
        post {
          entity(as[EventDescription]) { eventDescr =>
            onSuccess(createEvent(eventName, eventDescr.ticketCount)) {
              case BoxOffice.Out.EventCreated(event) =>
                complete(Created, EventDescription(event.ticketCount))
              case BoxOffice.Out.EventExists =>
                val err = Error(s"$eventName event exists already")
                complete(BadRequest, err)
              case unexpected =>
                val err = Error(s"Unexpected msg: $unexpected")
                complete(BadRequest, err)
            }
          }
        } ~
          get {
            onSuccess(getEvent(eventName)) {
              case BoxOffice.Out.Event(_, ticketCount) =>
                complete(OK, EventDescription(ticketCount))
              case BoxOffice.Out.EventMissing =>
                val err = Error(s"$eventName event missing")
                complete(BadRequest, err)
              case unexpected =>
                val err = Error(s"Unexpected msg: $unexpected")
                complete(BadRequest, err)
            }
          } ~
          get {
            onSuccess(getEvent(eventName)) {
              case BoxOffice.Out.Event(_, ticketCount) =>
                complete(OK, EventDescription(ticketCount))
              case BoxOffice.Out.EventMissing =>
                val err = Error(s"$eventName event missing")
                complete(BadRequest, err)
              case unexpected =>
                val err = Error(s"Unexpected msg: $unexpected")
                complete(BadRequest, err)
            }
          } ~
          delete {
            onSuccess(cancelEvent(eventName)) {
              case BoxOffice.Out.Event(_, ticketCount) =>
                complete(OK, EventDescription(ticketCount))
              case BoxOffice.Out.EventMissing =>
                val err = Error(s"$eventName event missing")
                complete(BadRequest, err)
              case unexpected =>
                val err = Error(s"Unexpected msg: $unexpected")
                complete(BadRequest, err)
            }
          }
      }
    } ~
      pathPrefix("events") {
        pathEndOrSingleSlash {
          get {
            onSuccess(getEvents) {
              case BoxOffice.Out.Events(events) =>
                complete(OK, events)
              case unexpected =>
                val err = Error(s"Unexpected msg: $unexpected")
                complete(BadRequest, err)
            }
          }
        }
      } ~
      pathPrefix("events" / PathMatchers.Segment / "tickets") { eventName =>
        pathEndOrSingleSlash {
          post {
            entity(as[TicketRequest]) { ticketReq =>
              onSuccess(buyTickets(eventName, ticketReq.ticketCount)) {
                case TicketSeller.Out.Tickets(_, tickets) =>
                  complete(OK, tickets)
                case BoxOffice.Out.EventMissing =>
                  val err = Error(s"$eventName event missing")
                  complete(BadRequest, err)
                case unexpected =>
                  val err = Error(s"Unexpected msg: $unexpected")
                  complete(BadRequest, err)
              }
            }
          }
        }
      }
  }

  private def createEvent(eventName: String, ticketCount: Int) = {
    (boxOffice ? BoxOffice.In.CreateEvent(eventName, ticketCount)).mapTo[RestApi.Response]
  }

  private def getEvent(eventName: String) = {
    (boxOffice ? BoxOffice.In.GetEvent(eventName)).mapTo[RestApi.Response]
  }

  private def getEvents = {
    (boxOffice ? BoxOffice.In.GetEvents).mapTo[RestApi.Response]
  }

  private def cancelEvent(eventName: String) = {
    (boxOffice ? BoxOffice.In.CancelEvent(eventName)).mapTo[RestApi.Response]
  }

  private def buyTickets(eventName: String, ticketCount: Int) = {
    (boxOffice ? BoxOffice.In.BuyTickets(eventName, ticketCount)).mapTo[RestApi.Response]
  }

}

object RestApi {

  trait Response

  case class EventDescription(ticketCount: Int)

  case class TicketRequest(ticketCount: Int)

  case class Error(msg: String)

}
