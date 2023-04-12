package com.github.zwrss.agents.servlets.behaviour

import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session
import play.api.libs.json.Json
import play.api.libs.json.Reads

abstract class GameSocketBehaviour[T: Reads] {

  protected def err(info: String): Nothing = throw new LogicError(info)

  private def host(implicit remote: RemoteEndpoint): String = remote.getInetSocketAddress.toString

  final def _onEnter()(implicit session: Session, remote: RemoteEndpoint): Unit = {
    println(s"Entering ${getClass.getSimpleName} $host")
    onEnter()
  }

  protected def onEnter()(implicit session: Session, remote: RemoteEndpoint): Unit = {}

  final def _onExit()(implicit session: Session, remote: RemoteEndpoint): Unit = {
    println(s"Leaving ${getClass.getSimpleName}")
    onExit()
  }

  protected def onExit()(implicit session: Session, remote: RemoteEndpoint): Unit = {}

  final def _onMessage(message: String)(implicit session: Session, remote: RemoteEndpoint): GameSocketBehaviour[_] = {
    try {
      println(s"Message $host : $message")
      val x = Json.parse(message).asOpt[T].getOrElse(err(s"Cannot deserialize $message"))
      onMessage(x)
    } catch {
      case LogicError(msg) =>
        println(s"Logic error [${remote.getInetSocketAddress.toString}]: $msg")
        session.getRemote.sendString(Json.obj("error" -> msg).toString)
        this
      case t: Throwable =>
        println(s"Internal error [${remote.getInetSocketAddress.toString}]: ")
        t.printStackTrace(System.out)
        session.getRemote.sendString(Json.obj("error" -> "Internal Error").toString)
        this
    }
  }

  protected def onMessage(message: T)(implicit session: Session, remote: RemoteEndpoint): GameSocketBehaviour[_] = this

}