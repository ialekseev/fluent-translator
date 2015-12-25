package com.smartelk.translator

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

    "providing a good text" should {
      "successfully get translations" in {
        (Translator give me many translations of "How are you?" from "en" to "ru" as future).futureValue.translations.length should be > 0
      }
    }
  }
}
