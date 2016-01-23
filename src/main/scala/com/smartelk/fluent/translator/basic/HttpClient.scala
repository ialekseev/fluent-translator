package com.smartelk.fluent.translator.basic

import com.ning.http.client.AsyncHttpClientConfig
import dispatch.{Http, Req, url}
import dispatch._, Defaults._

private[translator] object HttpClient {

  type KeyValueSeq = Seq[(String, String)]

  trait HttpClientResponseComposer[T] {
    def getResponse(http: Http, request: Req): Future[(Int, T)]
  }

  class HttpClient(withBuilder : (AsyncHttpClientConfig.Builder => com.ning.http.client.AsyncHttpClientConfig.Builder)){

    private val http = Http.configure(withBuilder)

    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: KeyValueSeq = Seq()): Future[(Int, T)] = {
      require(!r.uri.isEmpty)

      val basicRequest = composeBasicRequest(r)
      val request = basicRequest << body
      go(request.POST, implicitly[HttpClientResponseComposer[T]])
    }

    def post[T: HttpClientResponseComposer](r: HttpClientBasicRequest, body: String): Future[(Int, T)] = {
      require(!r.uri.isEmpty)
      require(!body.isEmpty)

      val basicRequest = composeBasicRequest(r)
      val request = basicRequest.setBody(body.getBytes)
      go(request.POST, implicitly[HttpClientResponseComposer[T]])
    }

    def get[T: HttpClientResponseComposer](r: HttpClientBasicRequest): Future[(Int, T)] = {
      require(!r.uri.isEmpty)

      go(composeBasicRequest(r).GET, implicitly[HttpClientResponseComposer[T]])
    }

    private def composeBasicRequest(r: HttpClientBasicRequest): Req = {
      val request = url(r.uri)
      val withHeaders = r.headers.foldLeft(request)((r, h)=> r.addHeader(h._1, h._2))
      val withQueryString = withHeaders <<? r.queryString
      withQueryString
    }

    private def go[T](request: Req, responseComposer: HttpClientResponseComposer[T]): Future[(Int, T)] = {
      responseComposer.getResponse(http, request)
    }
  }

  case class HttpClientBasicRequest(uri: String, queryString: KeyValueSeq = Seq(), headers: KeyValueSeq = Seq())

  implicit object HttpClientStringResponseComposer extends HttpClientResponseComposer[String]{
    def getResponse(http: Http, request: Req): Future[(Int, String)] = http(request > as.Response(r => (r.getStatusCode, r.getResponseBody)))
  }

  implicit object HttpClientBytesResponseComposer extends HttpClientResponseComposer[Array[Byte]]{
    def getResponse(http: Http, request: Req): Future[(Int, Array[Byte])] = http(request > as.Response(r => (r.getStatusCode, r.getResponseBodyAsBytes)))
  }
}
