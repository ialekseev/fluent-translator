package com.smartelk.fluent.translator.microsoft

import com.smartelk.fluent.translator.basic._

package object actions {
  val textSizeLimit = 10000

  def requireValidMicrosoftText(text: String) = {
    requireValidText(text)
    require(text.length <= textSizeLimit, s"The size of text to be translated must not exceed $textSizeLimit characters")
  }

  def requireValidMicrosoftCategory(category: String) = require(!category.isEmpty, "Category must not be empty")
}
