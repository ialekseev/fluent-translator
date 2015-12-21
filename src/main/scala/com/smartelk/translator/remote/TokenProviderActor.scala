package com.smartelk.translator.remote

import akka.actor.{Status, Actor}
import com.smartelk.translator.remote.HttpClient.HttpClient
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import scala.util.{Failure, Success, Try}

private[translator] object TokenProviderActor {

  class TokenProviderActor(clientId: String, clientSecret: String, val httpClient: HttpClient) extends Actor {
    implicit val json4sFormats = DefaultFormats
    val requestAccessTokenUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13"
    type Token = (Long, String)
    private var token: Token = (0L, "")

    private def getToken(): Try[Token] =  {
      if (System.currentTimeMillis() > token._1) {
        val now = System.currentTimeMillis()
        for {
          response <- Try {
            httpClient.post(requestAccessTokenUri, Seq("grant_type" -> "client_credentials",
              "client_id" -> clientId,
              "client_secret" -> clientSecret,
              "scope" -> "http://api.microsofttranslator.com"))
          }
          newToken <- response match {
            case (true, value) => {
              val newTokenJson = parse(value)
              val accessToken = (newTokenJson \ "access_token").extract[String]
              val expiresIn = (newTokenJson \ "expires_in").extract[Long]
              Success(now + expiresIn, accessToken)
            }
            case (false, message) => Failure(new Exception(s"Remote service returned a problem: $message"))
          }
        } yield newToken
      }
      Success(token)
    }

    override def receive: Receive = {
      case TokenRequestMessage => getToken() match {
        case Success(gottenToken) =>{
          token = gottenToken
          sender ! Status.Success(token._2)
        }
        case Failure(e) =>  sender ! Status.Failure(e)
      }
    }
  }

  case object TokenRequestMessage
}

