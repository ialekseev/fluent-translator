package com.smartelk.fluent.translator.microsoft

package object remote {
  val requestAccessTokenUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13"
  val translateUri = "http://api.microsofttranslator.com/V2/Http.svc/Translate"
  val getTranslationsUri = "http://api.microsofttranslator.com/V2/Http.svc/GetTranslations"
  val speakUri = "http://api.microsofttranslator.com/V2/Http.svc/Speak"

  val tokenExpirationDeltaInMillis = 60 * 1000
}
