package com.github.zwrss.agents.domain

import com.github.zwrss.agents.servlets.behaviour.GameClientBehaviour
import com.github.zwrss.agents.servlets.behaviour.GameClientRole
import com.github.zwrss.agents.servlets.behaviour.LogicError
import org.eclipse.jetty.websocket.api.RemoteEndpoint

class GameRoom(roomId: String) {

  private var users: Seq[GameClientBehaviour] = Seq.empty

  var state: GameState = GameState.random(roomId)

  def add(user: GameClientBehaviour): Unit = synchronized {
    if (user.getRole == GameClientRole.Admin && users.exists(_.getRole == GameClientRole.Admin)) {
      throw new LogicError("Only one admin can join a room!")
    }
    users :+= user
  }

  def remove(remote: RemoteEndpoint): Unit = synchronized {
    users = users.filterNot(_.getRemote == remote)
  }

  def reveal(word: String)(implicit remote: RemoteEndpoint): Unit = synchronized {
    users.find(_.getRemote == remote).foreach {
      case x if x.getRole != GameClientRole.Admin =>
        throw new LogicError("Only Admin can reveal words!")
      case _ =>
        state.reveal(word)
        broadcastState()
    }
  }

  def reset()(implicit remote: RemoteEndpoint): Unit = synchronized {
    users.find(_.getRemote == remote).foreach {
      case x if x.getRole != GameClientRole.Admin =>
        throw new LogicError("Only Admin can reset game!")
      case _ =>
        state = GameState.random(roomId)
        broadcastState()
    }
  }

  def isEmpty: Boolean = users.isEmpty

  def broadcastState(): Unit = users.foreach(_ sendGameState state)

}
