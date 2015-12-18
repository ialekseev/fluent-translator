package com.smartelk.translator.actions

import com.smartelk.translator.Dsl.{future}
import com.smartelk.translator.remote.RemoteService.RemoteServiceClient
import scala.concurrent.Future

private[translator] object GetTranslationsAction
{
  case class GetTranslationsRequest(text: String,
                              maxTranslations: Int,
                              fromLang: Option[String] = None,
                              toLang: Option[String] = None,
                              category: Option[String] = None) {
    requireValidText(text)
    require(maxTranslations > 0, "Maximum number of translations to return must be > 0")
  }

  class GetTranslationsActionState(val state: GetTranslationsRequest) extends ActionState[GetTranslationsRequest]{
    def from(lang: String) = {
      requireValidFrom(lang)
      new GetTranslationsActionStateFrom(state.copy(fromLang = Some(lang)))
    }
  }

  class GetTranslationsActionStateFrom(val state: GetTranslationsRequest) extends ActionState[GetTranslationsRequest]{
    def to(lang: String) = {
      requireValidTo(lang)
      new GetTranslationsActionStateTo(state.copy(toLang = Some(lang)))
    }
  }

  class GetTranslationsActionStateTo(val state: GetTranslationsRequest) extends ActionState[GetTranslationsRequest] {
    def withCategory(category: String) = {
      requireValidCategory(category)
      new GetTranslationsActionStateTo(state.copy(category = Some(category)))
    }

    def as(scalaFutureWord: future.type)(implicit client: RemoteServiceClient): Future[String] = ???
  }
}


