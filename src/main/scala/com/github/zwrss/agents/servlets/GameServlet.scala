package com.github.zwrss.agents.servlets

import com.github.zwrss.agents.servlets.GameServlet.GameSocket
import com.github.zwrss.agents.servlets.behaviour.GameSocketBehaviour
import com.github.zwrss.agents.servlets.behaviour.UninitializedBehaviour
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory

import scala.io.Source


class GameServlet(port: Int) extends WebSocketServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val resource = Source fromURL getClass.getClassLoader.getResource("gameroom.html") // wat?
    val html = resource.getLines().mkString("\n")
    resp setContentType "text/html"
    resp setStatus 200
    resp.getWriter.println(html)
  }

  def configure(factory: WebSocketServletFactory): Unit = {
    factory.getPolicy.setIdleTimeout(10000000)
    factory.register(classOf[GameSocket])
    factory.setCreator { (_, _) =>
      new GameSocket()
    }
  }

}

object GameServlet {

  class GameSocket extends WebSocketAdapter {

    private var behaviour: GameSocketBehaviour[_] = new UninitializedBehaviour()

    override def onWebSocketConnect(session: Session): Unit = {
      super.onWebSocketConnect(session)
      behaviour._onEnter()(getSession, getRemote)
    }

    override def onWebSocketClose(statusCode: Int, reason: String): Unit = {
      super.onWebSocketClose(statusCode, reason)
      behaviour._onExit()(getSession, getRemote)
    }

    override def onWebSocketText(message: String): Unit = {
      super.onWebSocketText(message)
      val newBehaviour = behaviour._onMessage(message)(getSession, getRemote)
      if (behaviour != newBehaviour) {
        behaviour._onExit()(getSession, getRemote)
        behaviour = newBehaviour
        newBehaviour._onEnter()(getSession, getRemote)
      }
    }

  }

}
