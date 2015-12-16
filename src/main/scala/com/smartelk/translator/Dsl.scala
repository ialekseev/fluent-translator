package com.smartelk.translator

import com.smartelk.translator.TranslateAction._

object Dsl {

  case object Translator {
    def translate(text: String) = new TranslateActionTextState(TranslateRequest(text))
  }

  case object autodetect
}
