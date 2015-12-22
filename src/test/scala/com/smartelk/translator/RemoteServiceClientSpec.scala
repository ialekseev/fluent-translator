package com.smartelk.translator

import akka.actor.{Props, ActorSystem, Status, Actor}
import akka.testkit.{TestKit}
import com.smartelk.translator.Dsl.`audio/wav`
import com.smartelk.translator.remote.HttpClient.{SuccessHttpResponse, ErrorHttpResponse, HttpClient}
import com.smartelk.translator.remote.RemoteServiceClient.{TranslateRequest, RemoteServiceClientImpl}
import com.smartelk.translator.remote.{RemoteServiceClient}
import com.smartelk.translator.remote.TokenProviderActor.{Token, TokenRequestMessage}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import scala.util.{Failure, Success}

class RemoteServiceClientSpec(system: ActorSystem) extends TestKit(system) with WordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll with ScalaFutures {
  def this() = this(ActorSystem("test"))

  val httpClient = mock[HttpClient]

  override def beforeEach() = {reset(httpClient)}
  override def afterAll {TestKit.shutdownActorSystem(system)}

  "Translating" when {
    "token provider returns failure" should {
      "fail with that failure" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Status.Failure(new RuntimeException("Bad!"))}
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, httpClient)

        //act
        whenReady(client.translate(TranslateRequest("bla", "en", "fr", None, None)).failed) {res =>

          //assert
          res.getMessage should be ("Bad!")
        }
      }
    }

    "gets an exception during calling a remote translation method" should {
      "fail with that exception" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)}
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, httpClient)

        when(httpClient.get(RemoteServiceClient.translateUri, Seq("Authorization"->"111aaa"), Seq("text"-> "bla", "from"->"en", "to"-> "fr", "contentType" -> `audio/wav`.toString, "category"-> "default"))).thenReturn(Failure(new RuntimeException("Can't connect")))

        //act
        whenReady(client.translate(TranslateRequest("bla", "en", "fr", Some(`audio/wav`.toString), Some("default"))).failed) {res =>

          //assert
          res.getMessage should be ("Can't connect")
        }
      }
    }

    "gets http error from a remote translation method" should {
      "fail with an exception" in {
        //arrange
        val actorRef = system.actorOf(Props(new Actor {
          def receive = {
            case TokenRequestMessage => sender ! Token("111aaa", 1000)
          }
        }))

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, httpClient)

        when(httpClient.get(RemoteServiceClient.translateUri, Seq("Authorization" -> "111aaa"), Seq("text" -> "bla", "from" -> "en", "to" -> "fr"))).thenReturn(Success(ErrorHttpResponse("Bad!")))

        //act
        whenReady(client.translate(TranslateRequest("bla", "en", "fr", None, None)).failed) { res =>

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

        val client = new RemoteServiceClientImpl("my-client-id", "my-client-secret", actorRef, httpClient)

        when(httpClient.get(RemoteServiceClient.translateUri, Seq("Authorization" -> "111aaa"), Seq("text" -> "blabla", "from" -> "en", "to" -> "fr"))).thenReturn(Success(SuccessHttpResponse("albalb")))

        //act
        whenReady(client.translate(TranslateRequest("blabla", "en", "fr", None, None))) { res =>

          //assert
          res should be ("albalb")
        }
      }
    }
  }
}
