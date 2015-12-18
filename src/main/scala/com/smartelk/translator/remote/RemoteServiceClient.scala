package com.smartelk.translator.remote

import akka.util.Timeout
import com.smartelk.translator.actions.TranslateAction._
import scala.concurrent.Future
import scala.util.Try
import scalaj.http.Http
import org.json4s._
import org.json4s.native.JsonMethods._
import akka.actor.{Props, ActorSystem, Actor}
import scala.concurrent.ExecutionContext.Implicits._
import akka.pattern.pipe
import akka.pattern.ask
import scala.concurrent.duration._

private[translator] object RemoteService {

  trait RemoteServiceClient {
    def translate(accessToken: String, request: TranslateRequest): Future[String]
  }

  trait RemoteServiceClientImpl extends RemoteServiceClient {
    val clientId: String
    val clientSecret: String

    private val system = ActorSystem()
    private val actor = system.actorOf(Props(new RemoteServiceClientActor(clientId, clientSecret)))
    implicit val defaultAskTimeout = Timeout(5.seconds)

    def translate(accessToken: String, request: TranslateRequest): Future[String] = {
      (actor ? TranslateMessage(accessToken, request)).map(_.asInstanceOf[String])
    }

    private case class RequestTokenResult(accessToken: String, expiresIn: Long)
    private case class TranslateMessage(accessToken: String, request: TranslateRequest)
    private class RemoteServiceClientActor(clientId: String, clientSecret: String) extends Actor {
      val requestAccessTokenUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13"
      type Token = (Long, String)

      //todo: safe token updating, e.g. use akka's state-transitions

      /*private var tokenState = (0L, "")
      private def getAccessToken(): Future[Token] = Future {
        if (System.currentTimeMillis() > tokenState._1) {
          val now = System.currentTimeMillis()
          val newTokenJson = parse(Http(requestAccessTokenUri).postForm(Seq("grant_type"->"client_credentials",
            "client_id"-> clientId,
            "client_secret"->clientSecret,
            "scope"->"http://api.microsofttranslator.com")).asString.body)

          val accessToken = (newTokenJson \ "access_token").extract[String]
          val expiresIn = (newTokenJson \ "expires_in").extract[Long]
          (now + expiresIn, accessToken)
        } else {
          tokenState
        }
      }*/

      private def translate(message: TranslateMessage): Future[String] = ???

      override def receive: Receive = {
        case message: TranslateMessage => translate(message) pipeTo sender
      }
    }
  }
}

