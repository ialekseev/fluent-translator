package com.smartelk.translator.remote

import scala.util.Try
import scalaj.http.{HttpResponse, Http}

private[translator] object HttpClient {

  type Response = (Boolean, String)
  type KeyValueSeq = Seq[(String, String)]

  trait HttpClient {
    def post(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response]
    def get(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response]
  }

  class HttpClientImpl extends HttpClient {
    def post(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response] = {
      require(!uri.isEmpty)
      go(() => Http(uri).headers(headers).postForm(params).asString)
    }


    def get(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response] = {
      require(!uri.isEmpty)
      go(() => Http(uri).headers(headers).params(params).asString)
    }

    def go(doRequest: ()=> HttpResponse[String]): Try[Response] = Try {
      val result = doRequest()
      (result.is2xx, result.body)
    }
  }

  object SuccessHttpResponse {
    def apply(value: String) = (true, value)
    def unapply(r: Response): Option[String] = if (r._1) Some(r._2) else None
  }

  object ErrorHttpResponse {
    def apply(problem: String) = (false, problem)
    def unapply(r: Response): Option[String] = if (!r._1) Some(r._2) else None
  }
}
