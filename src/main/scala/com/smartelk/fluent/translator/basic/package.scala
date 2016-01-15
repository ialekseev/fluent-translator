package com.smartelk.fluent.translator

import org.json4s.{DefaultFormats, CustomSerializer}
import org.json4s.JsonAST.{JLong, JString}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits._

package object basic {
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

  trait ActionState[S] {
    val state: S
  }

  trait InitialActionState extends ActionState[Unit] {
    val state = ()
  }
}
