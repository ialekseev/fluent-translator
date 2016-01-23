package com.smartelk.fluent.translator.microsoft

import scala.concurrent.Future

package object remote {
  val requestAccessTokenUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13"
  val translateUri = "http://api.microsofttranslator.com/V2/Http.svc/Translate"
  val getTranslationsUri = "http://api.microsofttranslator.com/V2/Http.svc/GetTranslations"
  val speakUri = "http://api.microsofttranslator.com/V2/Http.svc/Speak"

  val tokenExpirationDeltaInMillis = 60 * 1000

  def extractResponseBody[T](response: (Int, T)): Future[T] = response match {
    case (200, r) => Future.successful(r)
    case (xxx, e) => Future.failed(new RuntimeException(s"Remote service returned status: $xxx and body: $e"))
  }
}
