package com.smartelk.translator.remote

import scalaj.http.{Http}

private[translator] object HttpClient {

  type Response = (Boolean, String)

  trait HttpClient {
    def post(uri: String, params: Seq[(String, String)]): Response
    def get(uri: String, headers: Seq[(String, String)], params: Seq[(String, String)]): Response
  }

  class HttpClientImpl extends HttpClient {
    def post(uri: String, params: Seq[(String, String)]): Response = {
      val result = Http(uri).postForm(params).asString
      (result.isSuccess, result.body)
    }

    def get(uri: String, headers: Seq[(String, String)], params: Seq[(String, String)]): Response = {
      val result = Http(uri).headers(headers).params(params).asString
      (result.isSuccess, result.body)
    }
  }
}
