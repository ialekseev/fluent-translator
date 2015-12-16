package com.smartelk.translator

import com.smartelk.translator.Dsl.autodetect
import scala.concurrent.Future
import scalaz.concurrent.Task

private[translator] object TranslateAction
{
  case class TranslateRequest(textSetting: String,
                              fromLangSetting: Option[String] = None,
                              toLangSetting: Option[String] = Some("en"),
                              contentTypeSetting: Option[String] = None,
                              categorySetting: Option[String] = None) {

    private val textSizeLimit = 10000
    private val validContentTypes = Seq("text/plain", "text/html")

    require(textSetting.length < textSizeLimit, s"The size of text must not exceed $textSizeLimit characters")

    def from(lang: String): TranslateRequest = {
      require(!lang.isEmpty, "Language to translate FROM can't be empty")

      copy(fromLangSetting = Some(lang))
    }

    def from(auto: autodetect.type) = copy(fromLangSetting = None)

    def to(lang: String): TranslateRequest = {
      require(!lang.isEmpty, "Language to translate TO can't be empty")

      copy(toLangSetting = Some(lang))
    }

    def withContentType(contentType: String): TranslateRequest = {
      require(!contentType.isEmpty, "Content type can't be empty")
      require(validContentTypes.contains(contentType), "Provide a valid content type. Valid values:" + validContentTypes.toString())

      copy(contentTypeSetting= Some(contentType))
    }

    def withCategory(category: String): TranslateRequest = {
      require(!category.isEmpty, "Category can't be empty")

      copy(categorySetting = Some(category))
    }
  }

  class TranslateActionTextState(tr: TranslateRequest){
    def from(lang: String) = new TranslateActionFromState(tr.from(lang))
    def from(auto: autodetect.type) = new TranslateActionFromState(tr.from(auto))
  }

  class TranslateActionFromState(tr: TranslateRequest){
    def to(lang: String) = new TranslateActionToState(tr.to(lang))
  }

  class TranslateActionToState(tr: TranslateRequest) {
    def withContentType(contentType: String) = new TranslateActionToState(tr.withContentType(contentType))
    def withCategory(category: String) = new TranslateActionToState(tr.withCategory(category))
    def asTask()(implicit translatorSettings: TranslatorSettings): Task[String] = ???
    def runAsScalaFuture()(implicit translatorSettings: TranslatorSettings): Future[String] = ???
  }
}


