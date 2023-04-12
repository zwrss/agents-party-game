package com.github.zwrss.agents.servlets.behaviour

case class LogicError(msg: String) extends RuntimeException(msg)
