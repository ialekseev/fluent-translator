package com.smartelk.translator

import com.smartelk.translator.actions.GetTranslationsAction.{GetTranslationsActionState, GetTranslationsRequest}
import com.smartelk.translator.actions.SpeakAction.{SpeaksActionState, SpeakRequest}
import com.smartelk.translator.actions.{ActionState, InitialActionState, TranslateAction}
import TranslateAction._
import com.smartelk.translator.remote.RemoteService.RemoteServiceClientImpl

object Dsl {

  object Translator {
    def give(meWord: me.type) = new GiveActionState
    def speak(text: String) = new SpeaksActionState(new SpeakRequest(text))
  }

  object one
  object a
  object many
  object of
  object me
  object translation
  object translations
  object future

  class GiveActionState extends InitialActionState {
    import Dsl.{translations => Translations}
    def a(translationWord: translation.type) = new OneTranslationActionState
    def one(translationWord: translation.type) = new OneTranslationActionState
    def two(translationsWord: Translations.type) = new ManyTranslationsActionState(2)
    def three(translationsWord: Translations.type) = new ManyTranslationsActionState(3)
    def four(translationsWord: Translations.type) = new ManyTranslationsActionState(4)
    def five(translationsWord: Translations.type) = new ManyTranslationsActionState(5)
    def six(translationsWord: Translations.type) = new ManyTranslationsActionState(6)
    def seven(translationsWord: Translations.type) = new ManyTranslationsActionState(7)
    def eight(translationsWord: Translations.type) = new ManyTranslationsActionState(8)
    def nine(translationsWord: Translations.type) = new ManyTranslationsActionState(9)
    def ten(translationsWord: Translations.type) = new ManyTranslationsActionState(10)
    def translations(max: Int) = new ManyTranslationsActionState(max)
  }

  class OneTranslationActionState extends InitialActionState {
    def of(text: String) = new TranslateActionState(TranslateRequest(text))
  }

  class ManyTranslationsActionState(val state: Int) extends ActionState[Int] {
    def of(text: String) = new GetTranslationsActionState(GetTranslationsRequest(text, maxTranslations = state))
  }

  trait TextContentType
  case object `text/plain` extends TextContentType
  case object `text/html` extends TextContentType

  trait AudioContentType
  case object `audio/wav` extends AudioContentType
  case object `audio/mp3` extends AudioContentType

  trait AudioQuality
  case object MaxQuality extends AudioQuality
  case object MinSize extends AudioQuality

  type TranslatorClient = RemoteServiceClientImpl
}