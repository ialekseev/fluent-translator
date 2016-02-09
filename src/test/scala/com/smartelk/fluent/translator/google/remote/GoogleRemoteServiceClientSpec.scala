package com.smartelk.fluent.translator.google.remote

import com.smartelk.fluent.translator.Dsl.{`text/html`, `text/plain`}
import com.smartelk.fluent.translator.basic.HttpClient.{HttpClientBasicRequest, HttpClient}
import com.smartelk.fluent.translator.google.remote.GoogleRemoteServiceClient.{TranslateRequest, RemoteServiceClientImpl}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future

class GoogleRemoteServiceClientSpec extends WordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach with ScalaFutures {

  val httpClient = mock[HttpClient]
  val client = new RemoteServiceClientImpl("my-api-key", httpClient)
  override def beforeEach() = {reset(httpClient)}
  implicit override val patienceConfig  = PatienceConfig(timeout = Span(500, Millis))

  "Translating" when {
    "gets an exception during calling a remote method" should {
      "fail with that exception" in {
        //arrange
        when(httpClient.get[String](HttpClientBasicRequest(translateUri, Seq("key" -> "my-api-key", "q" ->"bla", "target" -> "fr", "source" -> "en")))).thenReturn(Future.failed(new RuntimeException("Can't connect")))

        //act
        whenReady(client.translate(TranslateRequest("bla", "fr", Some("en"), None)).failed) { res =>

          //assert
          res.getMessage should be("Can't connect")
        }
      }
    }

    "remote service returns bad JSON" should {
      "fail" in {
        //arrange
        when(httpClient.get[String](HttpClientBasicRequest(translateUri, Seq("key" -> "my-api-key", "q" ->"bla", "target" -> "fr", "format" -> "html")))).thenReturn(Future.successful(200, "Bad"))

        //act
        whenReady(client.translate(TranslateRequest("bla", "fr", None, Some(`text/html`))).failed) { res =>

          //assert
          res.getMessage should be("Server returned 200, but I can't parse JSON. Response was: Bad")
        }
      }
    }

    "everything is fine" should {
      "return a translation" in {
        //arrange
        val gonnaReturn =
          """{
              "data": {
                      "translations": [
                          {
                            "translatedText": "Hallo Welt"
                          }
                        ]
                      }
              }"""

        when(httpClient.get[String](HttpClientBasicRequest(translateUri, Seq("key" -> "my-api-key", "q" ->"bla", "target" -> "fr", "format" -> "text")))).thenReturn(Future.successful(200, gonnaReturn))

        //act
        whenReady(client.translate(TranslateRequest("bla", "fr", None, Some(`text/plain`)))) { res =>

          //assert
          res should be ("Hallo Welt")
        }
      }
    }
  }
}
