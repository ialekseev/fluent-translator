package com.smartelk.fluent.translator.google.remote

import com.smartelk.fluent.translator.Dsl.{`text/html`, `text/plain`, TextContentType}
import com.smartelk.fluent.translator.basic.HttpClient.{HttpClientBasicRequest, HttpClient}
import com.smartelk.fluent.translator.basic._
import org.json4s.JValue
import scala.concurrent.Future
import org.json4s.native.JsonMethods._
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.Try

private[translator] object GoogleRemoteServiceClient {
  trait RemoteServiceClient {
    def translate(r: TranslateRequest): Future[String]
  }

  class RemoteServiceClientImpl(val apiKey: String, val httpClient: HttpClient) extends RemoteServiceClient {
    require(!apiKey.isEmpty)

    def translate(r: TranslateRequest): Future[String] = {
      require(!r.text.isEmpty)
      require(!r.toLang.isEmpty)
      require(r.fromLang.isEmpty || !r.fromLang.get.isEmpty)

      val contentType = r.contentType map {
        case `text/plain` => "text"
        case `text/html` => "html"
      }

      for {
        response <- httpClient.get[String](HttpClientBasicRequest(translateUri, {
          Seq("key" -> apiKey, "q" -> r.text, "target" -> r.toLang) ++:
            keyValueSeqFromOption(r.fromLang, "source") ++:
            keyValueSeqFromOption(contentType, "format")
        })).flatMap(extractResponseBody(_))

        translation <- Try {
          (parse(response) \ "data" \ "translations").extract[Seq[JValue]].headOption.map(t => (t \ "translatedText").extract[String]).get
        }.withFailureMapping(s"Server returned 200, but I can't parse JSON. Response was: $response").toFuture
      } yield translation
    }
  }

  case class TranslateRequest(text: String, toLang: String, fromLang: Option[String], contentType: Option[TextContentType])
}
