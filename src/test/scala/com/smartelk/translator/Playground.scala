package com.smartelk.translator

import java.io.ByteArrayInputStream
import java.net.Proxy.Type
import java.net.{InetSocketAddress}
import com.smartelk.translator.Dsl._
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.mock.MockitoSugar
import scala.util.{Success, Try}

class Playground extends WordSpecLike with Matchers with MockitoSugar with ScalaFutures {

  val config = ConfigFactory.load("playground").withFallback(ConfigFactory.load())
  val playgroundClientId = config.getString("playground.clientId")
  val playgroundClientSecret = config.getString("playground.clientSecret")
  val playgroundProxy = Try {config.getConfig("playground.proxy")} match {
    case Success(proxy) => Some(new java.net.Proxy(Type.HTTP, new InetSocketAddress(proxy.getString("host"), proxy.getInt("port"))))
    case _ => None
  }

  implicit object client extends TranslatorClient {
    val clientId = playgroundClientId
    val clientSecret = playgroundClientSecret
    override val proxy = playgroundProxy
  }

  implicit override val patienceConfig  = PatienceConfig(timeout = Span(client.tokenRequestTimeoutMillis, Millis))

  "Translating" when {

    "providing an invalid client id" should {
      "fail" in {
        implicit val client = new TranslatorClient {
          val clientId = "bad"
          val clientSecret = playgroundClientSecret
          override val proxy = playgroundProxy
        }

        (the [RuntimeException] thrownBy (Translator give me a translation of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be (true)
      }
    }

    "providing an invalid client secret" should {
      "fail" in {
        implicit val client = new TranslatorClient {
          val clientId = playgroundClientId
          val clientSecret = "bad"
          override val proxy = playgroundProxy
        }

        (the [RuntimeException] thrownBy (Translator give me a translation of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be (true)
      }
    }

    "providing jabberwocky text" should {
      "return the same text back" in {
        (Translator give me a translation of "qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds" from "en" to "ru" as future).futureValue should be ("qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds")
      }
    }

    "providing a good text" should {
      "successfully get a translation" in {
        (Translator give me a translation of "How are you?" from "en" to "fr" as future).futureValue should be ("Comment vas-tu?")
      }
    }

    "providing a good text and an explicit content-type (text/html) " should {
      "successfully get a translation" in {
        (Translator give me a translation of "How are you?" from "en" to "fr" withContentType `text/html` as future).futureValue should be ("Comment vas-tu?")
      }
    }

    "providing a good text and an explicit content-type (text/plain) " should {
      "successfully get a translation" in {
        (Translator give me a translation of "How are you?" from "en" to "fr" withContentType `text/plain` as future).futureValue should be ("Comment vas-tu?")
      }
    }

    "providing a good text and an explicit category (general) " should {
      "successfully get a translation" in {
        (Translator give me a translation of "How are you?" from "en" to "fr" withCategory "general" as future).futureValue should be ("Comment vas-tu?")
      }
    }
  }

  "Getting translations" when {
    "providing an invalid client id" should {
      "fail" in {
        implicit val client = new TranslatorClient {
          val clientId = "bad"
          val clientSecret = playgroundClientSecret
          override val proxy = playgroundProxy
        }

        (the [RuntimeException] thrownBy (Translator give me many translations of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be (true)
      }
    }

    "providing an invalid client secret" should {
      "fail" in {
        implicit val client = new TranslatorClient {
          val clientId = playgroundClientId
          val clientSecret = "bad"
          override val proxy = playgroundProxy
        }

        (the [RuntimeException] thrownBy (Translator give me translations(10) of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be (true)
      }
    }

    "providing jabberwocky text" should {
      "return the same text back" in {
        val result = (Translator give me many translations of "qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds" from "en" to "ru" as future).futureValue
        result.translations.length should be > 0
        result.translations(0).translation should be ("qweqwuiyqweqweasdasdbmbnmqweqwenbmbanbsadsds")
      }
    }

    "providing a good text and an explicit category (general)" should {
      "successfully get translations" in {
        (Translator give me many translations of "How are you?" from "en" to "ru" withCategory "general" as future).futureValue.translations.length should be > 0
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
        implicit val client = new TranslatorClient {
          val clientId = "bad"
          val clientSecret = playgroundClientSecret
          override val proxy = playgroundProxy
        }

        (the[RuntimeException] thrownBy (Translator speak "How are you?" in "en" as future).futureValue).getMessage.contains("invalid_client") should be(true)
      }
    }

    "providing an invalid client secret" should {
      "fail" in {
        implicit val client = new TranslatorClient {
          val clientId = playgroundClientId
          val clientSecret = "bad"
          override val proxy = playgroundProxy
        }

        (the [RuntimeException] thrownBy (Translator speak "How are you?" in "en" as future).futureValue).getMessage.contains("invalid_client") should be (true)
      }
    }

    "providing only required parameters" should {
      "successfully get a pronunciation" in {
        val bytes = (Translator speak "How are you doing?" in "en" as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }

    "providing an explicit audio content type(audio/wav)" should {
      "successfully get a pronunciation" in {
        val bytes = (Translator speak "How are you doing?" in "en" withAudioContentType `audio/wav` as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }

    "providing an explicit audio content type(audio/mp3)" should {
      "successfully get a pronunciation" in {
        val bytes = (Translator speak "How are you doing?" in "en" withAudioContentType `audio/mp3` as future).futureValue.data

        bytes should not be empty
      }
    }

    "providing an explicit quality(MaxQuality)" should {
      "successfully get a pronunciation" in {
        val bytes = (Translator speak "How are you doing?" in "en" withQuality MaxQuality as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }

    "providing an explicit quality(MinSize)" should {
      "successfully get a pronunciation" in {
        val bytes = (Translator speak "How are you doing?" in "en" withQuality MinSize as future).futureValue.data

        bytes should not be empty

        speakWav(bytes)
      }
    }
  }
}
