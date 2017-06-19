package com.is

import com.typesafe.config.ConfigFactory

class Boot extends App {
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getString("http.port")
}
