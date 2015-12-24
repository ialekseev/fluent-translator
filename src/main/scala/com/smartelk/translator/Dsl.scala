package com.smartelk.translator

import akka.actor.{Props, ActorSystem}
import com.smartelk.translator.actions.GetTranslationsAction.{GetTranslationsActionState, GetTranslationsActionParams}
import com.smartelk.translator.actions.SpeakAction.{SpeaksActionState, SpeakActionParams}
import com.smartelk.translator.actions.{ActionState, InitialActionState, TranslateAction}
import TranslateAction._
import com.smartelk.translator.remote.HttpClient._
import com.smartelk.translator.remote.RemoteServiceClient.{RemoteServiceClient, RemoteServiceClientImpl}
import com.smartelk.translator.remote.TokenProviderActor.TokenProviderActor

object Dsl {

  object Translator {
    def give(meWord: me.type) = new GiveActionState
    def speak(text: String) = new SpeaksActionState(new SpeakActionParams(text))
  }

  object me
  object translation
  object future
  object translations

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

  trait TextContentType
  case object `text/plain` extends TextContentType
  case object `text/html` extends TextContentType

  trait AudioContentType
  case object `audio/wav` extends AudioContentType
  case object `audio/mp3` extends AudioContentType

  trait AudioQuality
  case object MaxQuality extends AudioQuality
  case object MinSize extends AudioQuality

  val translatorActorSystem = ActorSystem("microsoft-translator-scala-api")

  type TranslatorHttpClient = HttpClientImpl
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