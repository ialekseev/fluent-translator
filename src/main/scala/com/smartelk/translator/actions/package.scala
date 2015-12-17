package com.smartelk.translator

package object actions {
  val textSizeLimit = 10000

  def requireValidText(text: String) = {
    require(!text.isEmpty, "Text to be translated must not be empty")
    require(text.length <= textSizeLimit, s"The size of text to be translated must not exceed $textSizeLimit characters")
  }

  def requireValidFrom(from: String) = require(!from.isEmpty, "Language to translate FROM must not be empty")
  def requireValidTo(to: String) =  require(!to.isEmpty, "Language to translate TO must not be empty")
  def requireValidCategory(category: String) = require(!category.isEmpty, "Category must not be empty")

  trait ActionState[S] {
    val state: S
  }

  trait InitialActionState extends ActionState[Unit] {
    val state = ()
  }
}
