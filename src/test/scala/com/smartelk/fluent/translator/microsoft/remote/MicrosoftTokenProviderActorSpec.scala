package com.smartelk.fluent.translator.microsoft.remote

import akka.actor.{ActorSystem, Props, Status}
import akka.testkit.{ImplicitSender, TestKit}
import com.smartelk.fluent.translator.basic.HttpClient.{HttpClient, _}
import com.smartelk.fluent.translator.microsoft.remote.MicrosoftTokenProviderActor.{Token, TokenProviderActor, TokenRequestMessage}
import org.json4s.ParserUtil.ParseException
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import scala.concurrent.Future

class MicrosoftTokenProviderActorSpec(system: ActorSystem) extends TestKit(system) with ImplicitSender with WordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll {
  def this() = this(ActorSystem("test"))

  val httpClient = mock[HttpClient]

  override def beforeEach() = {reset(httpClient)}
  override def afterAll {TestKit.shutdownActorSystem(system)}

  val params = Seq("grant_type" -> "client_credentials", "client_id" -> "my-client-id", "client_secret" -> "my-client-secret", "scope" -> "http://api.microsofttranslator.com")

  "Requesting token for the first time" when {

    "remote service throws exception" should {
      "fail with that exception" in {
        //arrange
        val actor = system.actorOf(Props(new TokenProviderActor("my-client-id", "my-client-secret", httpClient)))
        when(httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), params)).thenReturn(Future.failed(new RuntimeException("Can't connect")))

        //act
        actor ! TokenRequestMessage

        //assert
        expectMsgType[Status.Failure].cause.getMessage should be ("Can't connect")
      }
    }

    "remote service returns success but json is invalid" should {
      "fail with ParseException" in {
        //arrange
        val actor = system.actorOf(Props(new TokenProviderActor("my-client-id", "my-client-secret", httpClient)))
        when(httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), params)).thenReturn(Future.successful("Bad json"))

        //act
        actor ! TokenRequestMessage

        //assert
        expectMsgType[Status.Failure].cause shouldBe a [ParseException]
      }
    }

    "remote service returns success and json is good" should {
      "get token" in {
        //arrange
        val actor = system.actorOf(Props(new TokenProviderActor("my-client-id", "my-client-secret", httpClient){
          override def getCurrentTimeMillis = 2001
        }))
        when(httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), params)).thenReturn(Future.successful("""{"access_token": "123abc", "expires_in": "1001"}"""))

        //act
        actor ! TokenRequestMessage

        //assert
        expectMsgType[Token] should be (Token("123abc", 2001 + 1001 * 1000 - tokenExpirationDeltaInMillis))
      }
    }
  }

  "Requesting token" when {
    "current token is still valid" should {
      "just return it" in {
        //arrange
        var currentTime = 2000
        val actor = system.actorOf(Props(new TokenProviderActor("my-client-id", "my-client-secret", httpClient){
          override def getCurrentTimeMillis = currentTime
        }))
        when(httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), params)).thenReturn(Future.successful("""{"access_token": "123abc", "expires_in": "600"}"""))
        actor ! TokenRequestMessage
        expectMsgType[Token] should be (Token("123abc", 2000 + 600 * 1000 - tokenExpirationDeltaInMillis))

        currentTime =  2000 + 600 * 1000 - tokenExpirationDeltaInMillis - 1
        when(httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), params)).thenReturn(Future.successful("""{"access_token": "123abc_new", "expires_in": "1000"}"""))

        //act
        actor ! TokenRequestMessage

        //assert
        expectMsgType[Token] should be (Token("123abc", 2000 + 600 * 1000 - tokenExpirationDeltaInMillis))
        verify(httpClient, times(1)).post[String](any[HttpClientBasicRequest], any[Seq[(String, String)]])(any[HttpClientResponseComposer[String]])
      }
    }

    "current token has expired" should {
      "get a new token" in {
        //arrange
        var currentTime = 2000
        val actor = system.actorOf(Props(new TokenProviderActor("my-client-id", "my-client-secret", httpClient){
          override def getCurrentTimeMillis = currentTime
        }))
        when(httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), params)).thenReturn(Future.successful("""{"access_token": "123abc", "expires_in": "600"}"""))
        actor ! TokenRequestMessage
        expectMsgType[Token] should be (Token("123abc", 2000 + 600 * 1000 - tokenExpirationDeltaInMillis))

        currentTime =  2000 + 600 * 1000 - tokenExpirationDeltaInMillis + 1
        when(httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), params)).thenReturn(Future.successful("""{"access_token": "123abc_new", "expires_in": "1500"}"""))

        //act
        actor ! TokenRequestMessage

        //assert
        expectMsgType[Token] should be (Token("123abc_new", currentTime + 1500 * 1000 - tokenExpirationDeltaInMillis))
      }
    }
  }
}
