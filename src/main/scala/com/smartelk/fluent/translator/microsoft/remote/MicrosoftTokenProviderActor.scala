package com.smartelk.fluent.translator.microsoft.remote

import akka.actor.{Actor, Status}
import com.smartelk.fluent.translator.basic.HttpClient.{HttpClient, _}
import com.smartelk.fluent.translator.basic._
import org.json4s.native.JsonMethods._
import scala.concurrent.{Future}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits._

private[translator] object MicrosoftTokenProviderActor {

  class TokenProviderActor(clientId: String, clientSecret: String, httpClient: HttpClient) extends Actor {
    private var token = Token("", 0L)
    def getCurrentTimeMillis = System.currentTimeMillis()

    private def requestToken(nowMillis: Long): Future[Token] =  {
        for {
          response <- httpClient.post[String](HttpClientBasicRequest(requestAccessTokenUri), Seq(
              "grant_type" -> "client_credentials",
              "client_id" -> clientId,
              "client_secret" -> clientSecret,
              "scope" -> "http://api.microsofttranslator.com"))
          newToken <-  Future {
              val newTokenJson = parse(response)
              val accessToken = (newTokenJson \ "access_token").extract[String]
              val expiresInSeconds = (newTokenJson \ "expires_in").extract[Long]
              val expiresInMillis = expiresInSeconds * 1000
              Token(accessToken, nowMillis + expiresInMillis - tokenExpirationDeltaInMillis)
            }
        } yield newToken
    }

    override def receive: Receive = {

      case TokenRequestMessage => {
        val nowMillis = getCurrentTimeMillis
        if (nowMillis > token.expiresMillis) {
          val selfActor = self
          val senderActor = sender
          requestToken(nowMillis) onComplete {
            case Success(token) => {
              selfActor ! UpdateTokenMessage(token)
              senderActor ! token
            }
            case Failure(f) => senderActor ! Status.Failure(f)
          }
        }
        else {
          sender ! token
        }
      }

      case UpdateTokenMessage(newToken) => {
        token = newToken
      }
    }
  }

  case class Token(accessToken: String, expiresMillis: Long)
  case object TokenRequestMessage
  case class UpdateTokenMessage(newToken: Token)
}

