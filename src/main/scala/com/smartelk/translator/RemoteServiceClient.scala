package com.smartelk.translator

import com.smartelk.translator.actions.TranslateAction

import scalaz.concurrent.Task
import TranslateAction._

trait RemoteServiceClient {
  def requestToken(clientId: String, clientSecret: String): Task[RequestTokenResult]
  def translate(accessToken: String, request: TranslateRequest): Task[String]
}

class RemoteServiceClientImpl extends RemoteServiceClient {
  def requestToken(clientId: String, clientSecret: String): Task[RequestTokenResult] = ???
  def translate(accessToken: String, request: TranslateRequest): Task[String] = ???
}

case class RequestTokenResult(accessToken: String, expiresIn: String)