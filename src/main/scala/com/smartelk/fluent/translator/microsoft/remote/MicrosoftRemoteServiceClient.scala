package com.smartelk.fluent.translator.microsoft.remote

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.smartelk.fluent.translator.Dsl.{AudioContentType, AudioQuality, TextContentType}
import com.smartelk.fluent.translator.microsoft.remote.MicrosoftTokenProviderActor.{TokenRequestMessage, Token}
import com.smartelk.fluent.translator.basic.HttpClient.{HttpClient, _}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.xml.XML

private[translator] object MicrosoftRemoteServiceClient {

  trait RemoteServiceClient {
    def translate(r: TranslateRequest): Future[String]
    def getTranslations(r: GetTranslationsRequest): Future[GetTranslationsResponse]
    def speak(r: SpeakRequest): Future[SpeakResponse]
  }

  class RemoteServiceClientImpl(val clientId: String, val clientSecret: String, val tokenProviderActor: ActorRef, val tokenRequestTimeoutMillis: Int, val httpClient: HttpClient) extends RemoteServiceClient {
    implicit val defaultAskTimeout = Timeout(tokenRequestTimeoutMillis.millis)

    def translate(r: TranslateRequest): Future[String] = {
      require(!r.text.isEmpty)
      require(!r.toLang.isEmpty)
      require(r.category.isEmpty || !r.category.get.isEmpty)

     for {
       body <- call(httpClient.get[String]) (HttpClientBasicRequest(translateUri,
          Seq("text" -> r.text, "to" -> r.toLang) ++: fromOption(r.fromLang, "from") ++: fromOption(r.contentType.map(_.toString), "contentType") ++: fromOption (r.category, "category")))
      } yield XML.loadString(body).text
     }

    def getTranslations(r: GetTranslationsRequest): Future[GetTranslationsResponse] = {
      require(!r.text.isEmpty)
      require(r.maxTranslations > 0)
      require(!r.fromLang.isEmpty)
      require(!r.toLang.isEmpty)
      require(r.category.isEmpty || !r.category.get.isEmpty)

      val requestBody = xml.Utility.trim(
      <TranslateOptions xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2">
        <Category>{r.category.getOrElse("")}</Category>
        <ContentType/>
        <ReservedFlags/>
        <State/>
        <Uri/>
        <User/>
      </TranslateOptions>).buildString(true)

      for {
        responseBody <- call(httpClient.post[String](_, requestBody))(HttpClientBasicRequest(getTranslationsUri,
          Seq("text" -> r.text, "from" -> r.fromLang, "to" -> r.toLang, "maxTranslations" -> r.maxTranslations.toString), Seq("Content-Type" -> "text/xml")))
        translationsXml = XML.loadString(responseBody) \ "Translations"
        translationMatchesXml <- {
          if (translationsXml.size > 0) Future.successful(translationsXml \ "TranslationMatch")
          else Future.failed(new RuntimeException(s"Remote service returned bad XML: $responseBody"))
        }
        translations = translationMatchesXml.map(t =>
          TranslationMatch((t \ "TranslatedText").text, (t \ "MatchDegree").text.toInt, (t \ "Rating").text.toInt, (t \ "Count").text.toInt))
      } yield GetTranslationsResponse(translations)
    }

    def speak(r: SpeakRequest): Future[SpeakResponse] = {
      require(!r.text.isEmpty)
      require(!r.lang.isEmpty)

      for {
        body <- call(httpClient.get[Array[Byte]])(
          HttpClientBasicRequest(speakUri, Seq("text" -> r.text, "language" -> r.lang) ++: fromOption(r.audioContentType.map(_.toString), "format") ++: fromOption(r.quality.map(_.toString), "options")))
      } yield SpeakResponse(body)
    }

    private def call[T](doRequest: HttpClientBasicRequest => Future[(Int, T)])(request: HttpClientBasicRequest): Future[T] = {
      (tokenProviderActor ? TokenRequestMessage).flatMap {
        case Token(accessToken, _) => doRequest(request.copy(headers = Seq("Authorization" -> ("Bearer " + accessToken)) ++: request.headers)).flatMap(extractResponseBody(_))
        case Status.Failure(e) => Future.failed(e)
      }
    }

    private def fromOption(op: Option[String], name: String): KeyValueSeq = op.map(f => Seq(name -> f)).getOrElse(Seq())
  }

  case class TranslateRequest(text: String, toLang: String, fromLang: Option[String], contentType: Option[TextContentType], category: Option[String])

  /*todo: Check if we can omit passing 'from' language to this API method(like in Translate method). Here(https://msdn.microsoft.com/en-us/library/ff512417.aspx) 'from' goes as a required parameter, but in GetTranslationsResponse it's being mentioned(indirectly) as an optional one*/
  case class GetTranslationsRequest(text: String, maxTranslations: Int, fromLang: String, toLang: String, category: Option[String])
  case class GetTranslationsResponse(translations: Seq[TranslationMatch])
  case class TranslationMatch(translation: String, matchDegree: Int, rating: Int, count: Int)

  case class SpeakRequest(text: String, lang: String, audioContentType: Option[AudioContentType], quality: Option[AudioQuality])
  case class SpeakResponse(data: Array[Byte])
}

