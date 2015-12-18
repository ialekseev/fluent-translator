package com.smartelk.translator.actions

import com.smartelk.translator.Dsl._
import com.smartelk.translator.remote.RemoteService.RemoteServiceClient
import scala.concurrent.Future

private[translator] object TranslateAction
{
  case class TranslateRequest(text: String,
                              fromLang: Option[String] = None,
                              toLang: Option[String] = None,
                              contentType: Option[TextContentType] = None,
                              category: Option[String] = None) {
    requireValidText(text)
  }

  class TranslateActionState(val state: TranslateRequest) extends ActionState[TranslateRequest]{
    def from(lang: String) = {
      requireValidFrom(lang)
      new TranslateActionStateFrom(state.copy(fromLang = Some(lang)))
    }

    def to(lang: String) = {
      new TranslateActionStateTo(state.copy(toLang = Some(lang)))
    }
  }

  class TranslateActionStateFrom(val state: TranslateRequest) extends ActionState[TranslateRequest]{
    def to(lang: String) = {
      requireValidTo(lang)
      new TranslateActionStateTo(state.copy(toLang = Some(lang)))
    }
  }

  class TranslateActionStateTo(val state: TranslateRequest) extends ActionState[TranslateRequest]{
    def withContentType(contentType: TextContentType) = {
      new TranslateActionStateTo(state.copy(contentType = Some(contentType)))
    }

    def withCategory(category: String) = {
      requireValidCategory(category)
      new TranslateActionStateTo(state.copy(category = Some(category)))
    }

    def as(scalaFutureWord: future.type)(implicit client: RemoteServiceClient): Future[String] = ???
  }
}


