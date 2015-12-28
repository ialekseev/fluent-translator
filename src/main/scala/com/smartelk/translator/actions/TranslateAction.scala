package com.smartelk.translator.actions

import com.smartelk.translator.Dsl._
import com.smartelk.translator.remote.RemoteServiceClient.TranslateRequest
import scala.concurrent.Future

private[translator] object TranslateAction {

  case class TranslateActionParams(text: String,
                              fromLang: Option[String] = None,
                              toLang: Option[String] = None,
                              contentType: Option[TextContentType] = None,
                              category: Option[String] = None) {
    requireValidText(text)
  }

  class TranslateActionState(val state: TranslateActionParams) extends ActionState[TranslateActionParams]{
    def from(lang: String) = {
      requireValidFrom(lang)
      new TranslateActionStateFrom(state.copy(fromLang = Some(lang)))
    }

    def to(lang: String) = {
      new TranslateActionStateTo(state.copy(toLang = Some(lang)))
    }
  }

  class TranslateActionStateFrom(val state: TranslateActionParams) extends ActionState[TranslateActionParams]{
    def to(lang: String) = {
      requireValidTo(lang)
      new TranslateActionStateTo(state.copy(toLang = Some(lang)))
    }
  }

  class TranslateActionStateTo(val state: TranslateActionParams) extends ActionState[TranslateActionParams]{
    def withContentType(contentType: TextContentType) = {
      new TranslateActionStateTo(state.copy(contentType = Some(contentType)))
    }

    def withCategory(category: String) = {
      requireValidCategory(category)
      new TranslateActionStateTo(state.copy(category = Some(category)))
    }

    def as(scalaFutureWord: future.type)(implicit client: TranslatorClient): Future[String] = {
      client.remoteServiceClient.translate(TranslateRequest(state.text, state.toLang.get, state.fromLang, state.contentType, state.category))
    }
  }
}


