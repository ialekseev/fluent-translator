package com.smartelk.translator

import scalaz.concurrent.Task
import com.smartelk.translator.TranslateAction._

trait RemoteServiceClient {
  def requestToken(clientId: String, clientSecret: String): Task[RequestTokenResult]
  def translate(accessToken: String, request: TranslateRequest): Task[String]
}

class RemoteServiceClientImpl extends RemoteServiceClient {
  def requestToken(clientId: String, clientSecret: String): Task[RequestTokenResult] = ???
  def translate(accessToken: String, request: TranslateRequest): Task[String] = ???
}

case class RequestTokenResult(accessToken: String, expiresIn: String)