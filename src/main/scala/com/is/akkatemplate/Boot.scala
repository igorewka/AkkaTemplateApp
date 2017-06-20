package com.is.akkatemplate

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object Boot extends App {

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem()
  val ec = system.dispatcher

  val restApi = ???
  implicit val materializer = ActorMaterializer()
  val bindingFuture = Http().bindAndHandle(restApi, host, port)

}
