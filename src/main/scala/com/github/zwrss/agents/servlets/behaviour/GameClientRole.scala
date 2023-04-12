package com.github.zwrss.agents.servlets.behaviour

import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.Reads

sealed trait GameClientRole

object GameClientRole {
  case object Admin extends GameClientRole
  case object Regular extends GameClientRole

  implicit def reads: Reads[GameClientRole] = {
    case JsString("Admin") => JsSuccess(Admin)
    case JsString("Regular") => JsSuccess(Regular)
    case x => JsError(s"$x is not a valid Role")
  }
}