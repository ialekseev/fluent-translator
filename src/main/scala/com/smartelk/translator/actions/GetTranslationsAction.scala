package com.smartelk.translator.actions

import com.smartelk.translator.Dsl.{scalaFuture, scalazTask}
import com.smartelk.translator.TranslatorSettings
import scala.concurrent.Future
import scalaz.concurrent.Task

private[translator] object GetTranslationsAction
{
  case class GetTranslationsRequest(text: String,
                              fromLang: Option[String] = None,
                              toLang: Option[String] = None,
                              maxTranslations: Option[Int] = None,
                              category: Option[String] = None) {
    requireValidText(text)
  }

  class GetTranslationsActionTextState(val state: GetTranslationsRequest) extends ActionState[GetTranslationsRequest]{
    def from(lang: String) = {
      requireValidFrom(lang)
      new GetTranslationsActionFromState(state.copy(fromLang = Some(lang)))
    }
  }

  class GetTranslationsActionFromState(val state: GetTranslationsRequest) extends ActionState[GetTranslationsRequest]{
    def to(lang: String) = {
      requireValidTo(lang)
      new GetTranslationsActionToState(state.copy(toLang = Some(lang)))
    }
  }

  class GetTranslationsActionToState(val state: GetTranslationsRequest) extends ActionState[GetTranslationsRequest] {
    def withMaxTranslations(max: Int) = {
      require(max > 0, "Maximum number of translations to return must be > 0")
      new GetTranslationsActionMaxTranslationsState(state.copy(maxTranslations = Some(max)))
    }
  }

  class GetTranslationsActionMaxTranslationsState(val state: GetTranslationsRequest) extends ActionState[GetTranslationsRequest] {
    def withCategory(category: String) = {
      requireValidCategory(category)
      new GetTranslationsActionMaxTranslationsState(state.copy(category = Some(category)))
    }

    def as(scalazTaskWord: scalazTask.type)(implicit translatorSettings: TranslatorSettings): Task[String] = ???
    def as(scalaFutureWord: scalaFuture.type)(implicit translatorSettings: TranslatorSettings): Future[String] = ???
  }
}


