package com.smartelk.translator.remote

import akka.util.Timeout
import com.smartelk.translator.remote.HttpClient.HttpClient
import com.smartelk.translator.remote.TokenProviderActor.TokenRequestMessage
import scala.concurrent.Future
import org.json4s._
import akka.actor._
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._

private[translator] object RemoteServiceClient {

  trait RemoteServiceClient {
    def translate(r: TranslateRequest): Future[String]
  }

  class RemoteServiceClientImpl(val clientId: String, val clientSecret: String, val tokenProviderActor: ActorRef, val httpClient: HttpClient) extends RemoteServiceClient {
    implicit val defaultAskTimeout = Timeout(5.seconds)
    implicit val json4sFormats = DefaultFormats
    val translateUri = "http://api.microsofttranslator.com/V2/Http.svc/Translate"

    def translate(r: TranslateRequest): Future[String] = {
      require(!r.text.isEmpty)
      require(!r.from.isEmpty)
      require(!r.to.isEmpty)
      require(r.contentType.isEmpty || !r.contentType.get.isEmpty)
      require(r.category.isEmpty || !r.category.get.isEmpty)

      (tokenProviderActor ? TokenRequestMessage).flatMap {
        case Status.Success(token) => Future {
            val params = Seq("text" -> r.text, "from" -> r.from, "to" -> r.to) ++: {
              if (r.contentType.isDefined) Seq("contentType"-> r.contentType.get) else Seq()
            } ++: {
              if (r.category.isDefined) Seq("category"-> r.category.get) else Seq()
            }
            httpClient.get(translateUri, Seq("Authorization" -> token.asInstanceOf[String]), params)._2
          }
        case Status.Failure(e) => Future.failed(e)
      }
    }
  }

  case class TranslateRequest(text: String, from: String, to: String, contentType: Option[String], category: Option[String])
}

