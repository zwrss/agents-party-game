package com.github.zwrss.agents.domain

import com.github.zwrss.agents.servlets.behaviour.GameClientBehaviour
import org.eclipse.jetty.websocket.api.RemoteEndpoint

import scala.collection.concurrent.TrieMap

object GameRooms {

  println("Initializing gamerooms")

  private val rooms: TrieMap[String, GameRoom] = TrieMap.empty

  def add(roomId: String, user: GameClientBehaviour): GameRoom = synchronized {
    println(s"Creating room with id $roomId")
    val room = rooms.getOrElseUpdate(roomId, new GameRoom(roomId))
    room.add(user)
    println(s"Added room to: ${rooms.keySet.mkString(", ")}")
    room
  }

  def remove(roomId: String, user: RemoteEndpoint): Unit = synchronized {
    rooms.get(roomId) foreach { room =>
      room.remove(user)
      if (room.isEmpty) {
        rooms.remove(roomId)
        println(s"Removing room with id $roomId")
      }
    }
  }

  def get(i: String): GameRoom = synchronized {
    rooms.getOrElse(i, sys.error(s"No room with id $i, available: ${rooms.keySet.mkString(", ")}"))
  }

  def isEmpty: Boolean = rooms.isEmpty

}
