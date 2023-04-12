package com.github.zwrss.agents.domain

import com.github.zwrss.agents.servlets.behaviour.GameClientRole
import play.api.libs.json.{Json, Writes, _}

import scala.io.Source
import scala.util.Random

class GameState(val roomId: String, val words: Seq[WordWithState]) {

  def reveal(word: String): Unit = words.find(_.word == word).foreach(_.reveal())

}

object GameState {
  implicit def writes(implicit role: GameClientRole): Writes[GameState] = (o: GameState) => Json.obj(
    "roomId" -> o.roomId,
    "words" -> o.words
  )

  def random(roomId: String): GameState = {
    val source = Source.fromResource("words.txt")
    val words = Random.shuffle(source.getLines()).take(25)
    val wordIndices = Random shuffle (0 until 25).toList
    val redWords = wordIndices.slice(0, 9).toSet // 9
    val blueWords = wordIndices.slice(9, 9 + 8).toSet // 8
    val blackWord = wordIndices.last // 1
    new GameState(
      roomId,
      words.zipWithIndex.map {
        case (word, index) =>
          val state = {
            if (redWords(index)) WordState.RedUnrevealed
            else if (blueWords(index)) WordState.BlueUnrevealed
            else if (blackWord == index) WordState.BlackUnrevealed
            else WordState.Unrevealed
          }
          new WordWithState(word, state)
      }.toSeq
    )
  }
}

class WordWithState(val word: String, var state: WordState) {
  def reveal(): Unit = {
    state = state.revealed
  }
}

object WordWithState {
  implicit def writes(implicit role: GameClientRole): Writes[WordWithState] = (o: WordWithState) => Json.obj(
    "word" -> o.word,
    "state" -> o.state
  )
}

sealed trait WordState {
  def revealed: RevealedWordState = WordState.Blank

  def obscured: WordState = WordState.Unrevealed
}

sealed trait RevealedWordState extends WordState {
  override def revealed: RevealedWordState = this

  override def obscured: WordState = this
}

object WordState {

  implicit def writes(implicit role: GameClientRole): Writes[WordState] = (o: WordState) => role match {
    case GameClientRole.Regular =>
      JsString(o.obscured.getClass.getSimpleName.stripSuffix("$"))
    case _ =>
      JsString(o.getClass.getSimpleName.stripSuffix("$"))
  }

  case object RedUnrevealed extends WordState {
    override def revealed: RevealedWordState = Red
  }

  case object Red extends RevealedWordState

  case object Unrevealed extends WordState

  case object Blank extends RevealedWordState

  case object BlueUnrevealed extends WordState {
    override def revealed: RevealedWordState = Blue
  }

  case object Blue extends RevealedWordState

  case object Black extends RevealedWordState

  case object BlackUnrevealed extends WordState {
    override def revealed: RevealedWordState = Black
  }

}
