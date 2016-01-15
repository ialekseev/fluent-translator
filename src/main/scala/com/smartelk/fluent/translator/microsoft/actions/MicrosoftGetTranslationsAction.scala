package com.smartelk.fluent.translator.microsoft.actions

import com.smartelk.fluent.translator.Dsl.Microsoft.TranslatorClient
import com.smartelk.fluent.translator.Dsl.future
import com.smartelk.fluent.translator.basic.ActionState
import com.smartelk.fluent.translator.microsoft.remote.MicrosoftRemoteServiceClient
import MicrosoftRemoteServiceClient.{GetTranslationsResponse, GetTranslationsRequest}
import scala.concurrent.Future

private[translator] object MicrosoftGetTranslationsAction {

  case class GetTranslationsActionParams(text: String,
                              maxTranslations: Int,
                              fromLang: Option[String] = None,
                              toLang: Option[String] = None,
                              category: Option[String] = None) {
    requireValidText(text)
    require(maxTranslations > 0, "Maximum number of translations to return must be > 0")
  }

  class GetTranslationsActionState(val state: GetTranslationsActionParams) extends ActionState[GetTranslationsActionParams]{
    def from(lang: String) = {
      requireValidFrom(lang)
      new GetTranslationsActionStateFrom(state.copy(fromLang = Some(lang)))
    }
  }

  class GetTranslationsActionStateFrom(val state: GetTranslationsActionParams) extends ActionState[GetTranslationsActionParams]{
    def to(lang: String) = {
      requireValidTo(lang)
      new GetTranslationsActionStateTo(state.copy(toLang = Some(lang)))
    }
  }

  class GetTranslationsActionStateTo(val state: GetTranslationsActionParams) extends ActionState[GetTranslationsActionParams] {
    def withCategory(category: String) = {
      requireValidCategory(category)
      new GetTranslationsActionStateTo(state.copy(category = Some(category)))
    }

    def as(scalaFutureWord: future.type)(implicit client: TranslatorClient): Future[GetTranslationsResponse] = {
      client.remoteServiceClient.getTranslations(GetTranslationsRequest(state.text, state.maxTranslations, state.fromLang.get, state.toLang.get, state.category))
    }
  }
}


