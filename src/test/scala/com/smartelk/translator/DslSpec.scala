package com.smartelk.translator

import com.smartelk.translator.Dsl._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import scala.util.Random

class DslSpec extends WordSpecLike with Matchers with MockitoSugar {
  implicit object client extends TranslatorClient {
    val clientId = "my-client-id"
    val clientSecret = "my-client-secret"
  }

  "TranslateAction" when {

    "constructing TranslateRequest with valid params" should {
      "do it properly" in {
        (Translator give me a translation of "blabla" to "ru").state should be (actions.TranslateAction.TranslateActionParams("blabla", None, Some("ru"), None, None))
        (Translator give me one translation of "blabla1" from "fr" to "en").state should be (actions.TranslateAction.TranslateActionParams("blabla1", Some("fr"), Some("en"), None, None))
        (Translator give me a translation of "blabla2" from "en" to "fr" withContentType `text/html` withCategory "general").state should be (actions.TranslateAction.TranslateActionParams("blabla2", Some("en"), Some("fr"), Some(`text/html`), Some("general")))
        (Translator give me one translation of "blabla2" from "en" to "fr" withContentType `text/plain`).state should be (actions.TranslateAction.TranslateActionParams("blabla2", Some("en"), Some("fr"), Some(`text/plain`)))
      }
    }

    "constructing TranslateRequest with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the [IllegalArgumentException] thrownBy (Translator give me a translation of "") should have message s"requirement failed: Text to be translated must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me one translation of Random.nextString(10001)) should have message s"requirement failed: The size of text to be translated must not exceed ${actions.textSizeLimit} characters"
        the [IllegalArgumentException] thrownBy (Translator give me a translation of "blabla" from "") should have message "requirement failed: Language to translate FROM must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me one translation of "blabla" from "en" to "") should have message "requirement failed: Language to translate TO must not be empty"
      }
    }
  }

  "GetTranslationsAction" when {

    "constructing GetTranslationsRequest with valid params" should {
      "do it properly" in {
        (Translator give me two translations of "blabla" from "fr" to "ru").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla", 2, Some("fr"), Some("ru"), None))
        (Translator give me three translations of "blabla1" from "fr" to "en").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla1", 3, Some("fr"), Some("en"), None))
        (Translator give me four translations of "blabla2" from "en" to "fr" withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla2", 4,  Some("en"), Some("fr"), Some("general")))
        (Translator give me five translations of "blabla2" from "en" to "fr" withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla2", 5, Some("en"), Some("fr"), Some("general")))
        (Translator give me six translations of "blabla2" from "en" to "fr" withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla2", 6, Some("en"), Some("fr"), Some("general")))
        (Translator give me seven translations of "blabla2" from "en" to "fr" withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla2", 7, Some("en"), Some("fr"), Some("general")))
        (Translator give me eight translations of "blabla2" from "en" to "fr" withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla2", 8, Some("en"), Some("fr"), Some("general")))
        (Translator give me nine translations of "blabla2" from "en" to "fr" withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla2", 9, Some("en"), Some("fr"), Some("general")))
        (Translator give me ten translations of "blabla2" from "en" to "fr" withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla2", 10, Some("en"), Some("fr"), Some("general")))
        (Translator give me translations(15) of "blabla" from "fr" to "ru").state should be (actions.GetTranslationsAction.GetTranslationsActionParams("blabla", 15, Some("fr"), Some("ru"), None))
      }
    }

    "constructing GetTranslationsRequest with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the [IllegalArgumentException] thrownBy (Translator give me two translations of "") should have message s"requirement failed: Text to be translated must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me three translations of Random.nextString(10001)) should have message s"requirement failed: The size of text to be translated must not exceed ${actions.textSizeLimit} characters"
        the [IllegalArgumentException] thrownBy (Translator give me four translations of "blabla" from "") should have message "requirement failed: Language to translate FROM must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me five translations of "blabla" from "en" to "") should have message "requirement failed: Language to translate TO must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me translations(0) of "blabla" from "en" to "ru") should have message "requirement failed: Maximum number of translations to return must be > 0"
        the [IllegalArgumentException] thrownBy (Translator give me translations(10) of "blabla" from "en" to "ru" withCategory "") should have message s"requirement failed: Category must not be empty"
      }
    }
  }

  "SpeakAction" when {

    "constructing SpeakRequest with valid params" should {
      "do it properly" in {
        (Translator speak "blabla" in "en").state should be(actions.SpeakAction.SpeakActionParams("blabla", Some("en"), None))
        (Translator speak "super" in "ru" withAudioContentType `audio/wav`).state should be(actions.SpeakAction.SpeakActionParams("super", Some("ru"), Some(`audio/wav`)))
        (Translator speak "super" in "en" withAudioContentType `audio/mp3`).state should be(actions.SpeakAction.SpeakActionParams("super", Some("en"), Some(`audio/mp3`)))
        (Translator speak "super" in "en" withQuality MaxQuality).state should be(actions.SpeakAction.SpeakActionParams("super", Some("en"), None, Some(MaxQuality)))
        (Translator speak "super" in "en" withQuality MinSize).state should be(actions.SpeakAction.SpeakActionParams("super", Some("en"), None, Some(MinSize)))
      }
    }

    "constructing SpeakRequest with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the [IllegalArgumentException] thrownBy (Translator speak "" in "en") should have message s"requirement failed: Text to be spoken must not be empty"
        the [IllegalArgumentException] thrownBy (Translator speak Random.nextString(2001) in "en") should have message s"requirement failed: The size of text to be spoken must not exceed ${actions.SpeakAction.speakTextSizeLimit} characters"
        the [IllegalArgumentException] thrownBy (Translator speak "blabla" in "") should have message "requirement failed: Language to speak IN must not be empty"
      }
    }
  }
}
