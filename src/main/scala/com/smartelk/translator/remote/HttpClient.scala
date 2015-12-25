package com.smartelk.translator.remote

import scala.util.Try
import scalaj.http.{HttpRequest, Http}

private[translator] object HttpClient {

  type Response = (Boolean, String)
  type KeyValueSeq = Seq[(String, String)]
  trait ParamsSeq

  trait HttpClient {
    def post(r: HttpClientBasicRequest, body: KeyValueSeq = Seq()): Try[Response]
    def post(r: HttpClientBasicRequest, body: String): Try[Response]
    def get(r: HttpClientBasicRequest): Try[Response]
  }

  class HttpClientImpl(connTimeoutMillis: Int, readTimeoutMillis: Int, proxy: Option[java.net.Proxy]) extends HttpClient {

    def post(r: HttpClientBasicRequest, body: KeyValueSeq = Seq()): Try[Response] = {
      require(!r.uri.isEmpty)
      go(Http(r.uri).headers(r.headers).params(r.queryString).postForm(body))
    }

    def post(r: HttpClientBasicRequest, body: String): Try[Response] = {
      require(!r.uri.isEmpty)
      require(!body.isEmpty)
      go(Http(r.uri).headers(r.headers).params(r.queryString).postData(body))
    }

    def get(r: HttpClientBasicRequest): Try[Response] = {
      require(!r.uri.isEmpty)
      go(Http(r.uri).headers(r.headers).params(r.queryString))
    }

    def go(request: HttpRequest): Try[Response] = Try {
      val withTimeouts = request.timeout(connTimeoutMs = connTimeoutMillis, readTimeoutMs = readTimeoutMillis)
      val withProxy = proxy.map(withTimeouts.proxy(_)).getOrElse(withTimeouts)
      val result = withProxy.asString
      (result.is2xx, result.body)
    }
  }

  case class HttpClientBasicRequest(uri: String, queryString: KeyValueSeq = Seq(), headers: KeyValueSeq = Seq())

  object SuccessHttpResponse {
    def apply(value: String) = (true, value)
    def unapply(r: Response): Option[String] = if (r._1) Some(r._2) else None
  }

  object ErrorHttpResponse {
    def apply(problem: String) = (false, problem)
    def unapply(r: Response): Option[String] = if (!r._1) Some(r._2) else None
  }
}
