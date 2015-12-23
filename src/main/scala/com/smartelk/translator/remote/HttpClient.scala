package com.smartelk.translator.remote

import scala.util.Try
import scalaj.http.{HttpRequest, Http}

private[translator] object HttpClient {

  type Response = (Boolean, String)
  type KeyValueSeq = Seq[(String, String)]
  trait ParamsSeq

  trait HttpClient {
    def post(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response]
    def get(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response]
  }

  class HttpClientImpl(connTimeoutMillis: Int, readTimeoutMillis: Int, proxy: Option[java.net.Proxy]) extends HttpClient {

    def post(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response] = {
      require(!uri.isEmpty)
      go(Http(uri).headers(headers).postForm(params))
    }


    def get(uri: String, params: KeyValueSeq, headers: KeyValueSeq): Try[Response] = {
      require(!uri.isEmpty)
      go(Http(uri).headers(headers).params(params))
    }

    def go(request: HttpRequest): Try[Response] = Try {
      val withTimeouts = request.timeout(connTimeoutMs = connTimeoutMillis, readTimeoutMs = readTimeoutMillis)
      val withProxy = proxy.map(withTimeouts.proxy(_)).getOrElse(withTimeouts)
      val result = withProxy.asString
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
