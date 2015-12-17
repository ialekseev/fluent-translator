package com.smartelk.translator

import com.smartelk.translator.actions.GetTranslationsAction.{GetTranslationsRequest, GetTranslationsActionTextState}
import com.smartelk.translator.actions.SpeakAction.{SpeakRequest, SpeaksActionTextState}
import com.smartelk.translator.actions.{InitialActionState, TranslateAction}
import TranslateAction._

object Dsl {

  object Translator {
    def give(meWord: me.type) = new GiveActionState
    def speak(text: String) = new SpeaksActionTextState(new SpeakRequest(text))
  }

  object autodetect
  object one
  object many
  object of
  object me
  object translation
  object translations
  object scalazTask
  object scalaFuture

  class GiveActionState extends InitialActionState {
    def one(translationWord: translation.type) = new OneTranslationActionState
    def many(translationsWord: translations.type) = new ManyTranslationsActionState
  }

  class OneTranslationActionState extends InitialActionState {
    def of(text: String) = new TranslateActionTextState(TranslateRequest(text))
  }

  class ManyTranslationsActionState extends InitialActionState {
    def of(text: String) = new GetTranslationsActionTextState(GetTranslationsRequest(text))
  }

  object TextContentType extends Enumeration {
    type TextContentType = Value
    val `text/plain`, `text/html` = Value
  }

  object AudioContentType extends Enumeration {
    type AudioContentType = Value
    val `audio/wav`, `audio/mp3` = Value
  }

  object AudioQuality extends Enumeration {
    type AudioQuality = Value
    val MaxQuality, MinSize = Value
  }
}