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

    "providing invalid client id" should {
      "fail" in {
        implicit val client = new TranslatorClient {
          val clientId = "bad"
          val clientSecret = playgroundClientSecret
          override val proxy = playgroundProxy
        }

        (the [RuntimeException] thrownBy (Translator give me a translation of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be (true)
      }
    }

    "providing invalid client secret" should {
      "fail" in {
        implicit val client = new TranslatorClient {
          val clientId = playgroundClientId
          val clientSecret = "bad"
          override val proxy = playgroundProxy
        }

        (the [RuntimeException] thrownBy (Translator give me a translation of "How are you?" from "en" to "fr" as future).futureValue).getMessage.contains("invalid_client") should be (true)
      }
    }

    "providing valid data" should {
      "successfully get a translation" in {
        (Translator give me a translation of "How are you?" from "en" to "fr" as future).futureValue should be ("Comment vas-tu?")
      }
    }
  }
}
