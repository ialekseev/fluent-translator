package com.smartelk.fluent.translator

import akka.actor.{Props, ActorSystem}
import com.smartelk.fluent.translator.basic.{ActionState, InitialActionState}
import com.smartelk.fluent.translator.basic.HttpClient._

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

  type TranslatorHttpClient = HttpClient
  type MicrosoftTranslatorClient = Microsoft.TranslatorClient
  type GoogleTranslatorClient = Google.TranslatorClient

  object Microsoft {
    import microsoft.actions.MicrosoftGetTranslationsAction.{GetTranslationsActionState, GetTranslationsActionParams}
    import microsoft.actions.MicrosoftSpeakAction.{SpeaksActionState, SpeakActionParams}
    import microsoft.actions.MicrosoftTranslateAction.{TranslateActionParams, TranslateActionState}
    import microsoft.remote.MicrosoftRemoteServiceClient.{RemoteServiceClient, RemoteServiceClientImpl}
    import microsoft.remote.MicrosoftTokenProviderActor.TokenProviderActor

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

      val connectTimeoutMillis = 1000
      val requestTimeoutMillis = 5000
      lazy val tokenRequestTimeoutMillis = requestTimeoutMillis + 1000
      lazy val httpClient = new HttpClient(_.setConnectTimeout(connectTimeoutMillis).setRequestTimeout(requestTimeoutMillis))
      lazy val tokenProviderActor = translatorActorSystem.actorOf(Props(new TokenProviderActor(clientId, clientSecret, httpClient)))
      lazy val remoteServiceClient: RemoteServiceClient = new RemoteServiceClientImpl(clientId, clientSecret, tokenProviderActor, tokenRequestTimeoutMillis, httpClient)
    }
  }

  object Google {
    import google.actions.GoogleTranslateAction.{TranslateActionParams, TranslateActionState}
    import google.remote.GoogleRemoteServiceClient.{RemoteServiceClient, RemoteServiceClientImpl}

    def give(meWord: me.type) = new GiveActionState

    class GiveActionState extends InitialActionState {
      def a(translationWord: translation.type) = new OneTranslationActionState
    }

    class OneTranslationActionState extends InitialActionState {
      def of(text: String) = new TranslateActionState(TranslateActionParams(text))
    }

    trait TranslatorClient {
      val apiKey: String

      val connectTimeoutMillis = 1000
      val requestTimeoutMillis = 5000
      lazy val httpClient = new HttpClient(_.setConnectTimeout(connectTimeoutMillis).setRequestTimeout(requestTimeoutMillis))
      lazy val remoteServiceClient: RemoteServiceClient = new RemoteServiceClientImpl(apiKey, httpClient)
    }
  }
}