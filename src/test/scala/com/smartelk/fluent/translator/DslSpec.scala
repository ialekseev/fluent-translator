package com.smartelk.fluent.translator

import com.smartelk.fluent.translator.Dsl._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import scala.concurrent.Future
import scala.util.Random

trait DslSpec extends WordSpecLike with Matchers with MockitoSugar with ScalaFutures

class MicrosoftDslSpec extends DslSpec {
  import microsoft.actions
  import microsoft.actions.MicrosoftGetTranslationsAction.GetTranslationsActionParams
  import microsoft.actions.MicrosoftSpeakAction
  import microsoft.actions.MicrosoftSpeakAction.SpeakActionParams
  import microsoft.actions.MicrosoftTranslateAction.TranslateActionParams
  import microsoft.remote.MicrosoftRemoteServiceClient._

  val serviceClient = mock[RemoteServiceClient]
  implicit object client extends MicrosoftTranslatorClient {
    val clientId = "my-client-id"
    val clientSecret = "my-client-secret"
    override lazy val remoteServiceClient = serviceClient
  }

  "TranslateAction" when {

    "constructing TranslateActionParams with valid arguments" should {
      "do it properly" in {
        (Microsoft give me a translation of "blabla" to "ru").state should be (TranslateActionParams("blabla", None, Some("ru"), None, None))
        (Microsoft give me one translation of "blabla1" from "fr" to "en").state should be (TranslateActionParams("blabla1", Some("fr"), Some("en"), None, None))
        (Microsoft give me a translation of "blabla2" from "en" to "fr" withContentType `text/html` withCategory "general").state should be (TranslateActionParams("blabla2", Some("en"), Some("fr"), Some(`text/html`), Some("general")))
        (Microsoft give me one translation of "blabla2" from "en" to "fr" withContentType `text/plain`).state should be (TranslateActionParams("blabla2", Some("en"), Some("fr"), Some(`text/plain`)))
      }
    }

    "constructing TranslateActionParams with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the [IllegalArgumentException] thrownBy (Microsoft give me a translation of "") should have message s"requirement failed: Text to be translated must not be empty"
        the [IllegalArgumentException] thrownBy (Microsoft give me one translation of Random.nextString(10001)) should have message s"requirement failed: The size of text to be translated must not exceed ${actions.textSizeLimit} characters"
        the [IllegalArgumentException] thrownBy (Microsoft give me a translation of "blabla" from "") should have message "requirement failed: Language to translate FROM must not be empty"
        the [IllegalArgumentException] thrownBy (Microsoft give me one translation of "blabla" from "en" to "") should have message "requirement failed: Language to translate TO must not be empty"
      }
    }

    "constructing TranslateActionParams with valid arguments as future" should {
     "1)call remote service client and get a translation" in {
       //arrange
       when(serviceClient.translate(TranslateRequest("blabla", "ru", None, None, None))).thenReturn(Future.successful("ablabl"))

       //act
       whenReady(Microsoft give me a translation of "blabla" to "ru" as future){res =>

         //assert
         res should be ("ablabl")
       }
     }

      "2)call remote service client and get a translation" in {
        //arrange
        when(serviceClient.translate(TranslateRequest("blabla", "ru", Some("en"), Some(`text/html`), Some("default")))).thenReturn(Future.successful("ablabl"))

        //act
        whenReady(Microsoft give me a translation of "blabla" from "en" to "ru" withContentType `text/html` withCategory "default" as future){res =>

          //assert
          res should be ("ablabl")
        }
      }
    }
  }

  "GetTranslationsAction" when {

    "constructing GetTranslationsActionParams with valid arguments" should {
      "do it properly" in {
        (Microsoft give me translations (2) of "blabla" from "fr" to "ru").state should be(GetTranslationsActionParams("blabla", 2, Some("fr"), Some("ru"), None))
        (Microsoft give me translations (10) of "blabla2" from "en" to "fr" withCategory "general").state should be(GetTranslationsActionParams("blabla2", 10, Some("en"), Some("fr"), Some("general")))
        (Microsoft give me many translations of "blabla" from "fr" to "ru").state should be(GetTranslationsActionParams("blabla", Microsoft.defaultManyTranslations, Some("fr"), Some("ru"), None))
      }
    }

    "constructing GetTranslationsActionParams with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the[IllegalArgumentException] thrownBy (Microsoft give me translations (2) of "") should have message s"requirement failed: Text to be translated must not be empty"
        the[IllegalArgumentException] thrownBy (Microsoft give me translations (3) of Random.nextString(10001)) should have message s"requirement failed: The size of text to be translated must not exceed ${actions.textSizeLimit} characters"
        the[IllegalArgumentException] thrownBy (Microsoft give me translations (4) of "blabla" from "") should have message "requirement failed: Language to translate FROM must not be empty"
        the[IllegalArgumentException] thrownBy (Microsoft give me many translations of "blabla" from "en" to "") should have message "requirement failed: Language to translate TO must not be empty"
        the[IllegalArgumentException] thrownBy (Microsoft give me translations (0) of "blabla" from "en" to "ru") should have message "requirement failed: Maximum number of translations to return must be > 0"
        the[IllegalArgumentException] thrownBy (Microsoft give me translations (10) of "blabla" from "en" to "ru" withCategory "") should have message s"requirement failed: Category must not be empty"
      }
    }

    "constructing GetTranslationsActionParams with valid arguments as future" should {
      "1)call remote service client and get translations" in {
        //arrange
        val gonnaRespond = GetTranslationsResponse(Seq(TranslationMatch("albalb", 40, 3, 1001)))
        when(serviceClient.getTranslations(GetTranslationsRequest("blabla", 3, "en", "ru", None))).thenReturn(Future.successful(gonnaRespond))

        //act
        whenReady(Microsoft give me translations (3) of "blabla" from "en" to "ru" as future) { res =>

          //assert
          res should be(gonnaRespond)
        }
      }

      "2)call remote service client and get translations" in {
        //arrange
        val gonnaRespond = GetTranslationsResponse(Seq(TranslationMatch("albalb", 40, 3, 1001)))
        when(serviceClient.getTranslations(GetTranslationsRequest("blabla", 2, "fr", "en", Some("default")))).thenReturn(Future.successful(gonnaRespond))

        //act
        whenReady(Microsoft give me translations (2) of "blabla" from "fr" to "en" withCategory "default" as future) { res =>

          //assert
          res should be(gonnaRespond)
        }
      }
    }
  }

  "SpeakAction" when {

      "constructing SpeakActionParams with valid arguments" should {
        "do it properly" in {
          (Microsoft speak "blabla" in "en").state should be(SpeakActionParams("blabla", Some("en"), None))
          (Microsoft speak "super" in "ru" withAudioContentType `audio/wav`).state should be(SpeakActionParams("super", Some("ru"), Some(`audio/wav`)))
          (Microsoft speak "super" in "en" withAudioContentType `audio/mp3`).state should be(SpeakActionParams("super", Some("en"), Some(`audio/mp3`)))
          (Microsoft speak "super" in "en" withQuality MaxQuality).state should be(SpeakActionParams("super", Some("en"), None, Some(MaxQuality)))
          (Microsoft speak "super" in "en" withQuality MinSize).state should be(SpeakActionParams("super", Some("en"), None, Some(MinSize)))
        }
      }

      "constructing SpeakActionParams with illegal arguments" should {
        "throw IllegalArgumentException" in {
          the[IllegalArgumentException] thrownBy (Microsoft speak "" in "en") should have message s"requirement failed: Text to be spoken must not be empty"
          the[IllegalArgumentException] thrownBy (Microsoft speak Random.nextString(2001) in "en") should have message s"requirement failed: The size of text to be spoken must not exceed ${MicrosoftSpeakAction.speakTextSizeLimit} characters"
          the[IllegalArgumentException] thrownBy (Microsoft speak "blabla" in "") should have message "requirement failed: Language to speak IN must not be empty"
        }
      }

      "constructing SpeakActionParams with valid arguments as future" should {
        "1)call remote service client and get audio" in {
          //arrange
          val gonnaRespond = SpeakResponse(Array[Byte](1))
          when(serviceClient.speak(SpeakRequest("blabla", "ru", None, None))).thenReturn(Future.successful(gonnaRespond))

          //act
          whenReady(Microsoft speak "blabla" in "ru" as future) { res =>

            //assert
            res should be(gonnaRespond)
          }
        }

        "2)call remote service client and get audio" in {
          //arrange
          val gonnaRespond = SpeakResponse(Array[Byte](1))
          when(serviceClient.speak(SpeakRequest("blabla", "ru", Some(`audio/wav`), Some(MinSize) ))).thenReturn(Future.successful(gonnaRespond))

          //act
          whenReady(Microsoft speak "blabla" in "ru" withAudioContentType `audio/wav` withQuality MinSize as future) { res =>

            //assert
            res should be(gonnaRespond)
          }
        }
      }
  }
}

class GoogleDslSpec extends DslSpec {
  import google.actions.GoogleTranslateAction.TranslateActionParams

  "TranslateAction" when {
    "constructing TranslateActionParams with valid arguments" should {
      "do it properly" in {
        (Google give me a translation of "blabla" to "ru").state should be (TranslateActionParams("blabla", None, Some("ru"), None))
        (Google give me a translation of "blabla2" from "en" to "fr" withContentType `text/html`).state should be (TranslateActionParams("blabla2", Some("en"), Some("fr"), Some(`text/html`)))
      }
    }

    "constructing TranslateActionParams with illegal arguments" should {
      "throw IllegalArgumentException" in {
        the [IllegalArgumentException] thrownBy (Google give me a translation of "") should have message s"requirement failed: Text to be translated must not be empty"
        the [IllegalArgumentException] thrownBy (Google give me a translation of "blabla" from "") should have message "requirement failed: Language to translate FROM must not be empty"
        the [IllegalArgumentException] thrownBy (Google give me a translation of "blabla" from "en" to "") should have message "requirement failed: Language to translate TO must not be empty"
      }
    }
  }
}