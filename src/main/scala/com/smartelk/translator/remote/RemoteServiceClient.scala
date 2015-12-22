package com.smartelk.translator.remote

import akka.util.Timeout
import com.smartelk.translator.remote.HttpClient._
import com.smartelk.translator.remote.TokenProviderActor.{Token, TokenRequestMessage}
import scala.concurrent.Future
import org.json4s._
import akka.actor._
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.Try

private[translator] object RemoteServiceClient {

  trait RemoteServiceClient {
    def translate(r: TranslateRequest): Future[String]
  }

  val translateUri = "http://api.microsofttranslator.com/V2/Http.svc/Translate"

  class RemoteServiceClientImpl(val clientId: String, val clientSecret: String, val tokenProviderActor: ActorRef, val httpClient: HttpClient) extends RemoteServiceClient {
    implicit val defaultAskTimeout = Timeout(5.seconds)
    implicit val json4sFormats = DefaultFormats

    def translate(r: TranslateRequest): Future[String] = {
      require(!r.text.isEmpty)
      require(!r.from.isEmpty)
      require(!r.to.isEmpty)
      require(r.contentType.isEmpty || !r.contentType.get.isEmpty)
      require(r.category.isEmpty || !r.category.get.isEmpty)

      call(translateUri)(httpClient.get) {
        Seq("text" -> r.text, "from" -> r.from, "to" -> r.to) ++: {
          if (r.contentType.isDefined) Seq("contentType"-> r.contentType.get) else Seq()
        } ++: {
          if (r.category.isDefined) Seq("category"-> r.category.get) else Seq()
        }
      }
    }

    private def call(uri: String)(func: (String, KeyValueSeq, KeyValueSeq) => Try[Response])(params: KeyValueSeq): Future[String] = {
      (tokenProviderActor ? TokenRequestMessage).flatMap {
        case Token(accessToken, _) => {
          tryToFuture(func(uri, Seq("Authorization" -> accessToken), params)).flatMap {
            case SuccessHttpResponse(result) => Future.successful(result)
            case ErrorHttpResponse(problem) => Future.failed(new RuntimeException(s"Remote service returned a problem: $problem"))
          }
        }
        case Status.Failure(e) => Future.failed(e)
      }
    }
  }

  case class TranslateRequest(text: String, from: String, to: String, contentType: Option[String], category: Option[String])
}

