package com.smartelk.translator

import com.smartelk.translator.Dsl._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import scala.util.Random

class DslSpec extends WordSpecLike with Matchers with MockitoSugar {
  implicit val settings = new TranslatorSettings("my-id", "my-secret")

  "TranslateAction" when {

    "constructing TranslateRequest with valid params" should {
      "do it properly" in {
        (Translator give me one translation of "blabla" from autodetect to "ru").state should be (actions.TranslateAction.TranslateRequest("blabla", None, Some("ru"), None, None))
        (Translator give me one translation of "blabla1" from "fr" to "en").state should be (actions.TranslateAction.TranslateRequest("blabla1", Some("fr"), Some("en"), None, None))
        (Translator give me one translation of "blabla2" from "en" to "fr" withContentType TextContentType.`text/html` withCategory "general").state should be (actions.TranslateAction.TranslateRequest("blabla2", Some("en"), Some("fr"), Some(TextContentType.`text/html`), Some("general")))
        (Translator give me one translation of "blabla2" from "en" to "fr" withContentType TextContentType.`text/plain`).state should be (actions.TranslateAction.TranslateRequest("blabla2", Some("en"), Some("fr"), Some(TextContentType.`text/plain`)))
      }
    }

    "constructing TranslateRequest with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the [IllegalArgumentException] thrownBy (Translator give me one translation of "") should have message s"requirement failed: Text to be translated must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me one translation of Random.nextString(10001)) should have message s"requirement failed: The size of text to be translated must not exceed ${actions.textSizeLimit} characters"
        the [IllegalArgumentException] thrownBy (Translator give me one translation of "blabla" from "") should have message "requirement failed: Language to translate FROM must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me one translation of "blabla" from "en" to "") should have message "requirement failed: Language to translate TO must not be empty"
      }
    }
  }

  "GetTranslationsAction" when {

    "constructing GetTranslationsRequest with valid params" should {
      "do it properly" in {
        (Translator give me many translations of "blabla" from "fr" to "ru" withMaxTranslations 10).state should be (actions.GetTranslationsAction.GetTranslationsRequest("blabla", Some("fr"), Some("ru"), Some(10), None))
        (Translator give me many translations of "blabla1" from "fr" to "en" withMaxTranslations 1).state should be (actions.GetTranslationsAction.GetTranslationsRequest("blabla1", Some("fr"), Some("en"), Some(1), None))
        (Translator give me many translations of "blabla2" from "en" to "fr" withMaxTranslations 5 withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsRequest("blabla2", Some("en"), Some("fr"), Some(5), Some("general")))
        (Translator give me many translations of "blabla2" from "en" to "fr" withMaxTranslations 2 withCategory "general").state should be (actions.GetTranslationsAction.GetTranslationsRequest("blabla2", Some("en"), Some("fr"), Some(2), Some("general")))
      }
    }

    "constructing GetTranslationsRequest with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the [IllegalArgumentException] thrownBy (Translator give me many translations of "") should have message s"requirement failed: Text to be translated must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me many translations of Random.nextString(10001)) should have message s"requirement failed: The size of text to be translated must not exceed ${actions.textSizeLimit} characters"
        the [IllegalArgumentException] thrownBy (Translator give me many translations of "blabla" from "") should have message "requirement failed: Language to translate FROM must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me many translations of "blabla" from "en" to "") should have message "requirement failed: Language to translate TO must not be empty"
        the [IllegalArgumentException] thrownBy (Translator give me many translations of "blabla" from "en" to "ru" withMaxTranslations  0) should have message "requirement failed: Maximum number of translations to return must be > 0"
        the [IllegalArgumentException] thrownBy (Translator give me many translations of "blabla" from "en" to "ru" withMaxTranslations 10 withCategory "") should have message s"requirement failed: Category must not be empty"
      }
    }
  }

  "SpeakAction" when {

    "constructing SpeakRequest with valid params" should {
      "do it properly" in {
        (Translator speak "blabla" in "en").state should be(actions.SpeakAction.SpeakRequest("blabla", Some("en"), None))
        (Translator speak "super" in "ru" withAudioContentType AudioContentType.`audio/wav`).state should be(actions.SpeakAction.SpeakRequest("super", Some("ru"), Some(AudioContentType.`audio/wav`)))
        (Translator speak "super" in "en" withAudioContentType AudioContentType.`audio/mp3`).state should be(actions.SpeakAction.SpeakRequest("super", Some("en"), Some(AudioContentType.`audio/mp3`)))
        (Translator speak "super" in "en" withQuality AudioQuality.MaxQuality).state should be(actions.SpeakAction.SpeakRequest("super", Some("en"), None, Some(AudioQuality.MaxQuality)))
        (Translator speak "super" in "en" withQuality AudioQuality.MinSize).state should be(actions.SpeakAction.SpeakRequest("super", Some("en"), None, Some(AudioQuality.MinSize)))
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
