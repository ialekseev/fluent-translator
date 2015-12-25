package com.smartelk.translator

import org.json4s.{DefaultFormats, CustomSerializer}
import org.json4s.JsonAST.{JLong, JString}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits._

package object remote {
  val requestAccessTokenUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13"
  val translateUri = "http://api.microsofttranslator.com/V2/Http.svc/Translate"
  val getTranslationsUri = "http://api.microsofttranslator.com/V2/Http.svc/GetTranslations"

  val tokenExpirationDeltaInMillis = 60 * 1000

  object StringToLong extends CustomSerializer[Long](format => ({ case JString(x) => x.toLong }, { case x: Long => JLong(x) }))
  implicit val json4sFormats = DefaultFormats + StringToLong

  def tryToFuture[T](t: => Try[T]): Future[T] = {
    Future {
      t
    }.flatMap {
      case Success(s) => Future.successful(s)
      case Failure(fail) => Future.failed(fail)
    }
  }
}
