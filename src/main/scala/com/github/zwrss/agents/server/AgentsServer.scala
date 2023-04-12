package com.github.zwrss.agents.server

import com.github.zwrss.agents.servlets.GameServlet
import com.github.zwrss.agents.servlets.ResourcesServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import scala.util.Try

object AgentsServer {

  val resourcesRoute = "/public/*"
  val gameRoute = "/"

  def main(args: Array[String]) = {

    val port = {
      args.headOption orElse sys.env.get("PORT") flatMap (a => Try(a.toInt).toOption) getOrElse 666
    }

    val server = new Server(port)

    val handler = new ServletContextHandler(ServletContextHandler.SESSIONS)


    handler.setContextPath("/")
    server.setHandler(handler)

    handler.addServlet(new ServletHolder(new ResourcesServlet), resourcesRoute)
    handler.addServlet(new ServletHolder(new GameServlet(port)), gameRoute)

    val sessionHandler = new SessionHandler
    sessionHandler.setUsingCookies(true)
    handler.setSessionHandler(sessionHandler)

    server.start()
    println(s"Server started on port $port with endpoints: '$gameRoute'")
    server.join()
  }


}
