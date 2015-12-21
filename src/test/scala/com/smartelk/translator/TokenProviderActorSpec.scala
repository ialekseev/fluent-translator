package com.smartelk.translator

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.smartelk.translator.remote.HttpClient.HttpClient
import com.smartelk.translator.remote.TokenProviderActor.TokenProviderActor
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

class TokenProviderActorSpec(system: ActorSystem) extends TestKit(system) with ImplicitSender with WordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll {
  def this() = this(ActorSystem("test"))

  val httpClient = mock[HttpClient]
  val tokenProviderTestActor = system.actorOf(Props(new TokenProviderActor("my-client-id", "my-client-secret", httpClient)))

  "TokenProviderActor" when {
    "requesting token" should {
      "fail" in {
        //todo
      }
    }
  }
}
