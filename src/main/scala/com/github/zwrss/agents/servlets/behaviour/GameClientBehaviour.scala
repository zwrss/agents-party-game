package com.github.zwrss.agents.servlets.behaviour

import com.github.zwrss.agents.domain.GameRooms
import com.github.zwrss.agents.domain.GameState
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session
import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.libs.json.Reads

class GameClientBehaviour(roomId: String, role: GameClientRole) extends GameSocketBehaviour[GameClientBehaviour.Message] {

  implicit def getRole: GameClientRole = role

  private var _remote: RemoteEndpoint = _

  def getRemote: RemoteEndpoint = _remote

  def sendGameState(state: GameState): Unit = {
    getRemote.sendString(Json.toJson(state).toString)
  }

  override protected def onEnter()(implicit session: Session, remote: RemoteEndpoint): Unit = {
    _remote = remote
    getRemote.sendString(Json.toJson(GameRooms.get(roomId).state).toString)
  }

  override protected def onMessage(message: GameClientBehaviour.Message)(implicit session: Session, remote: RemoteEndpoint): GameSocketBehaviour[_] = message match {
    case GameClientBehaviour.Message(Some(word), None) =>
      val room = GameRooms.get(roomId)
      room.reveal(word)
      this
    case GameClientBehaviour.Message(None, Some(GameClientBehaviour.Action.Reset)) =>
      val room = GameRooms.get(roomId)
      room.reset()
      this
    case m =>
      err(s"Cannot handle $m")
  }

  override protected def onExit()(implicit session: Session, remote: RemoteEndpoint): Unit = {
    GameRooms.remove(roomId, getRemote)
  }

}

object GameClientBehaviour {

  case class Message(word: Option[String], action: Option[Action])

  object Message {
    implicit def reads: Reads[Message] = Json.using[Json.WithDefaultValues].reads
  }

  trait Action

  object Action {

    case object Reset extends Action

    implicit def reads: Reads[Action] = {
      case JsString("Reset") => JsSuccess(Reset)
      case x => JsError(s"$x is not a valid action")
    }
  }

}
