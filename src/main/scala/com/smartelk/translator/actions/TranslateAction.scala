package com.smartelk.translator.actions

import com.smartelk.translator.Dsl.TextContentType.TextContentType
import com.smartelk.translator.Dsl.{scalaFuture, scalazTask, autodetect}
import com.smartelk.translator.TranslatorSettings
import scala.concurrent.Future
import scalaz.concurrent.Task

private[translator] object TranslateAction
{
  case class TranslateRequest(text: String,
                              fromLang: Option[String] = None,
                              toLang: Option[String] = None,
                              contentType: Option[TextContentType] = None,
                              category: Option[String] = None) {
    requireValidText(text)
  }

  class TranslateActionTextState(val state: TranslateRequest) extends ActionState[TranslateRequest]{
    def from(lang: String) = {
      requireValidFrom(lang)
      new TranslateActionFromState(state.copy(fromLang = Some(lang)))
    }

    def from(auto: autodetect.type) = {
      new TranslateActionFromState(state.copy(fromLang = None))
    }
  }

  class TranslateActionFromState(val state: TranslateRequest) extends ActionState[TranslateRequest]{
    def to(lang: String) = {
      requireValidTo(lang)
      new TranslateActionToState(state.copy(toLang = Some(lang)))
    }
  }

  class TranslateActionToState(val state: TranslateRequest) extends ActionState[TranslateRequest]{
    def withContentType(contentType: TextContentType) = {
      new TranslateActionToState(state.copy(contentType = Some(contentType)))
    }

    def withCategory(category: String) = {
      requireValidCategory(category)
      new TranslateActionToState(state.copy(category = Some(category)))
    }

    def as(scalazTaskWord: scalazTask.type)(implicit translatorSettings: TranslatorSettings): Task[String] = ???
    def as(scalaFutureWord: scalaFuture.type)(implicit translatorSettings: TranslatorSettings): Future[String] = ???
  }
}


