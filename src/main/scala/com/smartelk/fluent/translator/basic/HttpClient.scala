package com.smartelk.fluent.translator.basic

import scala.util.Try
import scalaj.http.{HttpRequest, Http}

private[translator] object HttpClient {

  type Response[T] = Either[String, T]
  type KeyValueSeq = Seq[(String, String)]

  trait HttpClientResponseComposer[T] {
    def getResponse(request: Any): Response[T]
  }

  trait HttpClient {
    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: KeyValueSeq = Seq()): Try[Response[T]]
    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: String): Try[Response[T]]
    def get[T: HttpClientResponseComposer](r: HttpClientBasicRequest): Try[Response[T]]
  }

  class HttpClientImpl(connTimeoutMillis: Int, readTimeoutMillis: Int, proxy: Option[java.net.Proxy]) extends HttpClient {

    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: KeyValueSeq = Seq()): Try[Response[T]] = {
      require(!r.uri.isEmpty)
      go(Http(r.uri).headers(r.headers).params(r.queryString).postForm(body), implicitly[HttpClientResponseComposer[T]])
    }

    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: String): Try[Response[T]] = {
      require(!r.uri.isEmpty)
      require(!body.isEmpty)
      go(Http(r.uri).headers(r.headers).params(r.queryString).postData(body), implicitly[HttpClientResponseComposer[T]])
    }

    def get[T: HttpClientResponseComposer](r: HttpClientBasicRequest): Try[Response[T]] = {
      require(!r.uri.isEmpty)
      go(Http(r.uri).headers(r.headers).params(r.queryString), implicitly[HttpClientResponseComposer[T]])
    }

    def go[T](request: HttpRequest, responseComposer: HttpClientResponseComposer[T]): Try[Response[T]] = Try {
      val withTimeouts = request.timeout(connTimeoutMs = connTimeoutMillis, readTimeoutMs = readTimeoutMillis)
      val withProxy = proxy.map(withTimeouts.proxy(_)).getOrElse(withTimeouts)
      responseComposer.getResponse(withProxy)
    }
  }

  implicit object HttpClientStringResponseComposer extends HttpClientResponseComposer[String]{
    def getResponse(request: Any): Response[String] = {
      val response = request.asInstanceOf[HttpRequest].asString
      if (response.is2xx) Right(response.body) else Left(response.body)
    }
  }

  implicit object HttpClientBytesResponseComposer extends HttpClientResponseComposer[Array[Byte]]{
    def getResponse(request: Any): Response[Array[Byte]] = {
      val response = request.asInstanceOf[HttpRequest].asBytes
      if (response.is2xx) Right(response.body) else Left(response.toString)
    }
  }

  case class HttpClientBasicRequest(uri: String, queryString: KeyValueSeq = Seq(), headers: KeyValueSeq = Seq())

  object SuccessHttpResponse {
    def apply[T](value: T) = Right(value)
    def unapply[T](r: Response[T]): Option[T] = r match {
      case Right(r) => Some(r)
      case Left(e) => None
    }
  }

  object ErrorHttpResponse {
    def apply[T](problem: T) = Left(problem)
    def unapply[T](r: Response[T]): Option[String] = r match {
      case Left(e) => Some(e)
      case Right(r) => None
    }
  }
}
