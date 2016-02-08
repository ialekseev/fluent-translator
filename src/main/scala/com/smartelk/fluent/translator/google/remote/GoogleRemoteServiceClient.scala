package com.smartelk.fluent.translator.google.remote

import com.smartelk.fluent.translator.Dsl.TextContentType
import com.smartelk.fluent.translator.basic.HttpClient.HttpClient
import scala.concurrent.Future

private[translator] object GoogleRemoteServiceClient {
  trait RemoteServiceClient {
    def translate(r: TranslateRequest): Future[String]
  }

  class RemoteServiceClientImpl(val apiKey: String, val httpClient: HttpClient) extends RemoteServiceClient {
    def translate(r: TranslateRequest): Future[String] = ???
  }

  case class TranslateRequest(text: String, toLang: String, fromLang: Option[String], contentType: Option[TextContentType])
}
