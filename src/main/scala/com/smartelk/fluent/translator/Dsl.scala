package com.smartelk.fluent.translator

import akka.actor.{Props, ActorSystem}
import com.smartelk.fluent.translator.basic.{ActionState, InitialActionState}
import com.smartelk.fluent.translator.microsoft.actions.MicrosoftGetTranslationsAction.{GetTranslationsActionState, GetTranslationsActionParams}
import com.smartelk.fluent.translator.microsoft.actions.MicrosoftSpeakAction.{SpeaksActionState, SpeakActionParams}
import com.smartelk.fluent.translator.microsoft.actions.MicrosoftTranslateAction.{TranslateActionParams, TranslateActionState}
import com.smartelk.fluent.translator.microsoft.remote.{MicrosoftTokenProviderActor, MicrosoftRemoteServiceClient}
import com.smartelk.fluent.translator.basic.HttpClient._
import MicrosoftRemoteServiceClient.{RemoteServiceClient, RemoteServiceClientImpl}
import MicrosoftTokenProviderActor.TokenProviderActor

object Dsl {
  val translatorActorSystem = ActorSystem("fluent-translator")

  object me
  object translation
  object translations
  object future

  trait TextContentType
  case object `text/plain` extends TextContentType
  case object `text/html` extends TextContentType

  trait AudioContentType
  case object `audio/wav` extends AudioContentType
  case object `audio/mp3` extends AudioContentType

  trait AudioQuality
  case object MaxQuality extends AudioQuality
  case object MinSize extends AudioQuality

  type TranslatorHttpClient = HttpClientImpl
  type MicrosoftTranslatorClient = Microsoft.TranslatorClient

  object Microsoft {
    def give(meWord: me.type) = new GiveActionState
    def speak(text: String) = new SpeaksActionState(new SpeakActionParams(text))

    val defaultManyTranslations = 100

    class GiveActionState extends InitialActionState {
      import Dsl.{translations => Translations}
      def a(translationWord: translation.type) = new OneTranslationActionState
      def one(translationWord: translation.type) = new OneTranslationActionState
      def many(translationsWord: Translations.type) = new ManyTranslationsActionState(defaultManyTranslations)
      def translations(max: Int) = new ManyTranslationsActionState(max)
    }

    class OneTranslationActionState extends InitialActionState {
      def of(text: String) = new TranslateActionState(TranslateActionParams(text))
    }

    class ManyTranslationsActionState(val state: Int) extends ActionState[Int] {
      def of(text: String) = new GetTranslationsActionState(GetTranslationsActionParams(text, maxTranslations = state))
    }

    trait TranslatorClient {
      val clientId: String
      val clientSecret: String

      val connTimeoutMillis = 1000
      val readTimeoutMillis = 5000
      val proxy: Option[java.net.Proxy] = None
      lazy val tokenRequestTimeoutMillis = readTimeoutMillis + 1000
      lazy val httpClient: HttpClient = new HttpClientImpl(connTimeoutMillis, readTimeoutMillis, proxy)
      lazy val tokenProviderActor = translatorActorSystem.actorOf(Props(new TokenProviderActor(clientId, clientSecret, httpClient)))
      lazy val remoteServiceClient: RemoteServiceClient = new RemoteServiceClientImpl(clientId, clientSecret, tokenProviderActor, tokenRequestTimeoutMillis, httpClient)
    }
  }
}