package com.github.zwrss.agents.servlets.behaviour

import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session
import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.libs.json.Reads

import scala.util.Random


class UninitializedBehaviour extends GameSocketBehaviour[UninitializedBehaviour.Message] {

  import UninitializedBehaviour._

  override protected def onMessage(message: Message)(implicit session: Session, remote: RemoteEndpoint): GameSocketBehaviour[_] = message match {
    case Message(Action.Create, _) =>
      val i = "%04d" format Random.nextInt(10000)
      remote.sendString(Json.obj("roomId" -> i).toString())
      new RoleSelectBehaviour(i)
    case Message(Action.Join, roomIdOpt) =>
      val roomId = roomIdOpt getOrElse err("Room ID is required to join")
      remote.sendString(Json.obj("roomId" -> roomId).toString())
      new RoleSelectBehaviour(roomId)
    case _ =>
      this
  }

}

object UninitializedBehaviour {

  case class Message(action: Action, roomId: Option[String])

  object Message {
    implicit def reads: Reads[Message] = Json.using[Json.WithDefaultValues].reads
  }

  sealed trait Action

  object Action {

    case object Create extends Action

    case object Join extends Action

    implicit def reads: Reads[Action] = {
      case JsString("Create") => JsSuccess(Create)
      case JsString("Join") => JsSuccess(Join)
      case x => JsError(s"$x is not a valid Action")
    }

  }

}