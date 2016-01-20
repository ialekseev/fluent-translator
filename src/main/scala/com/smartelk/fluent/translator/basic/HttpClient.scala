package com.smartelk.fluent.translator.basic

import com.ning.http.client.AsyncHttpClientConfig
import dispatch.{Http, Req, url}
import dispatch._, Defaults._

private[translator] object HttpClient {

  type KeyValueSeq = Seq[(String, String)]

  trait HttpClientResponseComposer[T] {
    def getResponse(http: Http, request: Req): Future[T]
  }

  class HttpClient(withBuilder : (AsyncHttpClientConfig.Builder => com.ning.http.client.AsyncHttpClientConfig.Builder)){

    private val http = Http.configure(withBuilder)

    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: KeyValueSeq = Seq()): Future[T] = {
      require(!r.uri.isEmpty)

      val basicRequest = composeBasicRequest(r)
      val request = body.foldLeft(basicRequest)((r, p)=> r.addParameter(p._1, p._2)).POST
      go(request, implicitly[HttpClientResponseComposer[T]])
    }

    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: String): Future[T] = {
      require(!r.uri.isEmpty)
      require(!body.isEmpty)

      val basicRequest = composeBasicRequest(r)
      val request = basicRequest.setBody(body).POST
      go(request, implicitly[HttpClientResponseComposer[T]])
    }

    def get[T: HttpClientResponseComposer](r: HttpClientBasicRequest): Future[T] = {
      require(!r.uri.isEmpty)

      go(composeBasicRequest(r), implicitly[HttpClientResponseComposer[T]])
    }

    private def composeBasicRequest(r: HttpClientBasicRequest): Req = {
      val request = url(r.uri)
      val withHeaders = r.headers.foldLeft(request)((r, h)=> r.addHeader(h._1, h._2))
      val withQueryString = r.queryString.foldLeft(withHeaders)((r, q)=> r.addQueryParameter(q._1, q._2))
      withQueryString
    }

    private def go[T](request: Req, responseComposer: HttpClientResponseComposer[T]): Future[T] = {
      responseComposer.getResponse(http, request)
    }
  }

  case class HttpClientBasicRequest(uri: String, queryString: KeyValueSeq = Seq(), headers: KeyValueSeq = Seq())

  implicit object HttpClientStringResponseComposer extends HttpClientResponseComposer[String]{
    def getResponse(http: Http, request: Req): Future[String] = {
      http.asInstanceOf[Http](request.asInstanceOf[Req] OK as.String)
    }
  }

  implicit object HttpClientBytesResponseComposer extends HttpClientResponseComposer[Array[Byte]]{
    def getResponse(http: Http, request: Req): Future[Array[Byte]] = {
      http.asInstanceOf[Http](request.asInstanceOf[Req] OK as.Bytes)
    }
  }
}
