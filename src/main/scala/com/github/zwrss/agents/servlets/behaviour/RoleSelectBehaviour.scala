package com.github.zwrss.agents.servlets.behaviour

import com.github.zwrss.agents.domain.GameRooms
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session
import play.api.libs.json.Json
import play.api.libs.json.Reads

class RoleSelectBehaviour(roomId: String) extends GameSocketBehaviour[RoleSelectBehaviour.Message] {

  override protected def onMessage(message: RoleSelectBehaviour.Message)(implicit session: Session, remote: RemoteEndpoint): GameSocketBehaviour[_] = {
    val client = new GameClientBehaviour(roomId, message.role)
    GameRooms.add(roomId, client)
    client
  }

}


object RoleSelectBehaviour {

  case class Message(role: GameClientRole)

  object Message {
    implicit def reads: Reads[Message] = Json.reads
  }

}