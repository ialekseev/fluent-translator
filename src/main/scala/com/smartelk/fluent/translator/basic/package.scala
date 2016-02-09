package com.smartelk.fluent.translator

import com.smartelk.fluent.translator.basic.HttpClient._
import org.json4s.{DefaultFormats, CustomSerializer}
import org.json4s.JsonAST.{JLong, JString}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

package object basic {
  object StringToLong extends CustomSerializer[Long](format => ({ case JString(x) => x.toLong }, { case x: Long => JLong(x) }))
  implicit val json4sFormats = DefaultFormats + StringToLong

  trait ActionState[S] {
    val state: S
  }

  trait InitialActionState extends ActionState[Unit] {
    val state = ()
  }

  def requireValidFrom(from: String) = require(!from.isEmpty, "Language to translate FROM must not be empty")
  def requireValidTo(to: String) =  require(!to.isEmpty, "Language to translate TO must not be empty")
  def requireValidText(text: String) = require(!text.isEmpty, "Text to be translated must not be empty")

  def extractResponseBody[T](response: (Int, T)): Future[T] = response match {
    case (200, r) => Future.successful(r)
    case (xxx, e) => Future.failed(new RuntimeException(s"Remote service returned status: $xxx and body: $e"))
  }

  def keyValueSeqFromOption(op: Option[String], name: String): KeyValueSeq = op.map(f => Seq(name -> f)).getOrElse(Seq())

  implicit class tryWrapper[T](t: Try[T]){
    def toFuture = t match {
      case Success(s) => Future.successful(s)
      case Failure(e) => Future.failed(e)
    }

    def withFailureMapping(message: String): Try[T] = t.recover { case e: Exception => throw new RuntimeException(message, e.getCause)}
  }
}
