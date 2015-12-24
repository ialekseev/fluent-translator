package com.smartelk.translator

import akka.actor.{Props, ActorSystem, Status, Actor}
import akka.testkit.{TestKit}
import com.smartelk.translator.Dsl.`audio/wav`
import com.smartelk.translator.remote._
import com.smartelk.translator.remote.HttpClient.{SuccessHttpResponse, ErrorHttpResponse, HttpClient}
import com.smartelk.translator.remote.RemoteServiceClient._
import com.smartelk.translator.remote.TokenProviderActor.{Token, TokenRequestMessage}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import scala.util.{Failure, Success}

class RemoteServiceClientSpec(system: ActorSystem) extends TestKit(system) with WordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll with ScalaFutures {
  def this() = this(ActorSystem("test"))

  val httpClient = mock[HttpClient]

  override def beforeEach() = {reset(httpClient)}
  override def afterAll {TestKit.shutdownActorSystem(system)}
  implicit override val patienceConfig  = PatienceConfig(timeout = Span(500, Millis))

  "Translating" when {
    "token provider returns failure" should {
      "fail with that failure" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Status.Failure(new RuntimeException("Bad!"))}
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        //act
        whenReady(client.translate(TranslateRequest("bla", "fr", None, None, None)).failed) {res =>

          //assert
          res.getMessage should be ("Bad!")
        }
      }
    }

    "gets an exception during calling a remote method" should {
      "fail with that exception" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)}
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        when(httpClient.get(translateUri, Seq("text"-> "bla", "to"-> "fr", "from"->"en", "contentType" -> `audio/wav`.toString, "category"-> "default"), Seq("Authorization"->"Bearer 111aaa"))).thenReturn(Failure(new RuntimeException("Can't connect")))

        //act
        whenReady(client.translate(TranslateRequest("bla", "fr", Some("en"), Some(`audio/wav`.toString), Some("default"))).failed) {res =>

          //assert
          res.getMessage should be ("Can't connect")
        }
      }
    }

    "gets http error from a remote method" should {
      "fail with an exception" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)
          }
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        when(httpClient.get(translateUri, Seq("text" -> "bla", "to" -> "fr", "from" -> "en"), Seq("Authorization" -> "Bearer 111aaa"))).thenReturn(Success(ErrorHttpResponse("Bad!")))

        //act
        whenReady(client.translate(TranslateRequest("bla", "fr", Some("en"), None, None)).failed) { res =>

          //assert
          res.getMessage should be("Remote service returned a problem: Bad!")
        }
      }
    }

    "everything is fine" should {
      "get a translation" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)
          }
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        when(httpClient.get(translateUri, Seq("text" -> "blabla", "to" -> "fr"), Seq("Authorization" -> "Bearer 111aaa"))).thenReturn(Success(SuccessHttpResponse("<string>albalb</string>")))

        //act
        whenReady(client.translate(TranslateRequest("blabla", "fr", None, None, None))) { res =>

          //assert
          res should be ("albalb")
        }
      }
    }
  }

  "Getting translations" when {
    "token provider returns failure" should {
      "fail with that failure" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Status.Failure(new RuntimeException("Bad!"))
          }
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        //act
        whenReady(client.getTranslations(GetTranslationsRequest("blabla", 10, "en", "fr", None)).failed) { res =>

          //assert
          res.getMessage should be("Bad!")
        }
      }
    }

    "gets an exception during calling a remote method" should {
      "fail with that exception" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)}
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        val options = xml.Utility.trim(<TranslateOptions xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2">
                                            <Category>super</Category>
                                          </TranslateOptions>).buildString(true)

        when(httpClient.post(getTranslationsUri, Seq("text" -> "bla", "from"-> "en", "to" -> "fr", "maxTranslations" -> "10", "options"-> options), Seq("Authorization"->"Bearer 111aaa"))).thenReturn(Failure(new RuntimeException("Can't connect")))

        //act
        whenReady(client.getTranslations(GetTranslationsRequest("bla", 10, "en", "fr", Some("super"))).failed) {res =>

          //assert
          res.getMessage should be ("Can't connect")
        }
      }
    }

    "gets http error from a remote method" should {
      "fail with an exception" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)
          }
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        when(httpClient.post(getTranslationsUri, Seq("text" -> "bla", "from"-> "en", "to" -> "fr", "maxTranslations" -> "10"), Seq("Authorization"->"Bearer 111aaa"))).thenReturn(Success(ErrorHttpResponse("Bad!")))

        //act
        whenReady(client.getTranslations(GetTranslationsRequest("bla", 10, "en", "fr", None)).failed) { res =>

          //assert
          res.getMessage should be("Remote service returned a problem: Bad!")
        }
      }
    }

    "everything is fine" should {
      "get a translation" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)
          }
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, 2000, httpClient)

        val gonnaReturn = xml.Utility.trim(
        <GetTranslationsResponse xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
          <Translations>
            <TranslationMatch>
              <Count>1001</Count>
              <MatchDegree>40</MatchDegree>
              <Rating>6</Rating>
              <TranslatedText>ablabl</TranslatedText>
            </TranslationMatch>
          </Translations>
        </GetTranslationsResponse>).buildString(true)

        when(httpClient.post(getTranslationsUri, Seq("text" -> "blabla", "from"-> "en", "to" -> "fr", "maxTranslations" -> "10"), Seq("Authorization" -> "Bearer 111aaa"))).thenReturn(Success(SuccessHttpResponse(gonnaReturn)))

        //act
        whenReady(client.getTranslations(GetTranslationsRequest("blabla", 10, "en", "fr", None))) { res =>

          //assert
          res should be (GetTranslationsResponse(Seq(TranslationMatch("ablabl", 40, 6, 1001))))
        }
      }
    }
  }
}
