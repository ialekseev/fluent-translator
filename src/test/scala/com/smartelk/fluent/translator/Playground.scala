package com.smartelk.fluent.translator

import java.io.ByteArrayInputStream
import com.smartelk.fluent.translator.Dsl._
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Span}
import org.scalatest.{Matchers, WordSpecLike}
import scala.util.Try

//!Important!
/* Playground - is a bunch of integration tests working with live Translator service. To make them work you need to create src/test/resources/playground.conf config with the following HOCON structure:
    playground {
      microsoft {
        clientId = "your client id"
        clientSecret = "your client secret"
      }
}
*/

//todo: fix broken specs: problem with HttpClient's post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: String) ???

trait Playground extends WordSpecLike with Matchers with MockitoSugar with ScalaFutures {
  case class HttpProxy(host: String, port: Int, user: Option[String] = None, password: Option[String] = None)

  val config = ConfigFactory.load("playground").withFallback(ConfigFactory.load())
  val playgroundProxy = Try(config.getConfig("playground.proxy")).toOption.map(c => new HttpProxy(c.getString("host"), c.getInt("port"), Try(c.getString("user")).toOption, Try(c.getString("password")).toOption))
  implicit override val patienceConfig  = PatienceConfig(timeout = Span(6000, Millis))
}

class MicrosoftPlayground extends Playground {
    val playgroundClientId = config.getString("playground.microsoft.clientId")
    val playgroundClientSecret = config.getString("playground.microsoft.clientSecret")

    implicit object client extends MicrosoftTranslatorClient {
      val clientId = playgroundClientId
      val clientSecret = playgroundClientSecret
    }

  "Translating" when {

    "providing an invalid client id" should {
      "fail" in {
        implicit val client = new MicrosoftTranslatorClient {
          val clientId = "bad"
          val clientSecret = playgroundClientSecret
        }

        (the[RuntimeException] thrownBy (Microsoft give me a translation of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be(true)
      }
    }

    "providing an invalid client secret" should {
      "fail" in {
        implicit val client = new MicrosoftTranslatorClient {
          val clientId = playgroundClientId
          val clientSecret = "bad"
        }

        (the[RuntimeException] thrownBy (Microsoft give me a translation of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be(true)
      }
    }

    "providing jabberwocky text" should {
      "return the same text back" in {
        (Microsoft give me a translation of "qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds" from "en" to "ru" as future).futureValue should be("qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds")
      }
    }

    "providing a good text" should {
      "successfully get a translation" in {
        (Microsoft give me a translation of "How are you?" from "en" to "fr" as future).futureValue should be("Comment vas-tu?")
      }
    }

    "providing a good text and an explicit content-type (text/html) " should {
      "successfully get a translation" in {
        (Microsoft give me a translation of "How are you?" from "en" to "fr" withContentType `text/html` as future).futureValue should be("Comment vas-tu?")
      }
    }

    "providing a good text and an explicit content-type (text/plain) " should {
      "successfully get a translation" in {
        (Microsoft give me a translation of "How are you?" from "en" to "fr" withContentType `text/plain` as future).futureValue should be("Comment vas-tu?")
      }
    }

    "providing a good text and an explicit category (general) " should {
      "successfully get a translation" in {
        (Microsoft give me a translation of "How are you?" from "en" to "fr" withCategory "general" as future).futureValue should be("Comment vas-tu?")
      }
    }
  }

  "Getting translations" when {
    "providing an invalid client id" should {
      "fail" in {
        implicit val client = new MicrosoftTranslatorClient {
          val clientId = "bad"
          val clientSecret = playgroundClientSecret
        }

        (the[RuntimeException] thrownBy (Microsoft give me many translations of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be(true)
      }
    }

    "providing an invalid client secret" should {
      "fail" in {
        implicit val client = new MicrosoftTranslatorClient {
          val clientId = playgroundClientId
          val clientSecret = "bad"
        }

        (the[RuntimeException] thrownBy (Microsoft give me translations (10) of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be(true)
      }
    }

    "providing jabberwocky text" should {
      "return the same text back" in {
        val result = (Microsoft give me many translations of "qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds" from "en" to "ru" as future).futureValue
        result.translations.length should be > 0
        result.translations(0).translation should be("qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds")
      }
    }

    "providing a good text and an explicit category (general)" should {
      "successfully get translations" in {
        (Microsoft give me many translations of "How are you?" from "en" to "ru" /*withCategory "general"*/ as future).futureValue.translations.length should be > 0
      }
    }
  }

  "Speaking" when {
    def speakWav(bytes: Array[Byte]) = {
      import sun.audio.{AudioPlayer, AudioStream}
      val audioStream = new AudioStream(new ByteArrayInputStream(bytes))
      AudioPlayer.player.start(audioStream)
      Thread.sleep(1000)
    }

    "providing an invalid client id" should {
      "fail" in {
        implicit val client = new MicrosoftTranslatorClient {
          val clientId = "bad"
          val clientSecret = playgroundClientSecret
        }

        (the[RuntimeException] thrownBy (Microsoft speak "How are you?" in "en" as future).futureValue).getMessage.contains("invalid_client") should be(true)
      }
    }

    "providing an invalid client secret" should {
      "fail" in {
        implicit val client = new MicrosoftTranslatorClient {
          val clientId = playgroundClientId
          val clientSecret = "bad"
        }

        (the[RuntimeException] thrownBy (Microsoft speak "How are you?" in "en" as future).futureValue).getMessage.contains("invalid_client") should be(true)
      }
    }

    "providing only required parameters" should {
      "successfully get a pronunciation" in {
        val bytes = (Microsoft speak "How are you doing?" in "en" as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }

    "providing an explicit audio content type(audio/wav)" should {
      "successfully get a pronunciation" in {
        val bytes = (Microsoft speak "How are you doing?" in "en" withAudioContentType `audio/wav` as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }

    "providing an explicit audio content type(audio/mp3)" should {
      "successfully get a pronunciation" in {
        val bytes = (Microsoft speak "How are you doing?" in "en" withAudioContentType `audio/mp3` as future).futureValue.data

        bytes should not be empty
      }
    }

    "providing an explicit quality(MaxQuality)" should {
      "successfully get a pronunciation" in {
        val bytes = (Microsoft speak "How are you doing?" in "en" withQuality MaxQuality as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }

    "providing an explicit quality(MinSize)" should {
      "successfully get a pronunciation" in {
        val bytes = (Microsoft speak "How are you doing?" in "en" withQuality MinSize as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }
  }
}

object Readme {
  /***README.md*/
  def readme = {
    import com.smartelk.fluent.translator.Dsl._

    implicit object client extends MicrosoftTranslatorClient {
      val clientId = "microsoft client id"
      val clientSecret = "microsoft client secret"
    }

    Microsoft give me a translation of "Comment vas-tu?" from "fr" to "en" as future //Future[String]
    Microsoft give me a translation of "What a lovely weather today!" from "en" to "fr" withContentType `text/html` as future //Future[String]
    Microsoft give me many translations of "Doing well by doing good" from "en" to "ru" as future //Future[GetTranslationsResponse]
    Microsoft give me translations(3) of "Paris holidays" from "en" to "ru" withCategory "general" as future //Future[GetTranslationsResponse]
    Microsoft speak "I'm doing well enough now" in "en" withAudioContentType `audio/mp3` as future //Future[SpeakResponse]
    Microsoft speak "How are you doing?" in "en" withQuality MinSize as future //Future[SpeakResponse]
  }
}
