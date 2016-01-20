package com.smartelk.fluent.translator

import org.json4s.{DefaultFormats, CustomSerializer}
import org.json4s.JsonAST.{JLong, JString}

package object basic {
  object StringToLong extends CustomSerializer[Long](format => ({ case JString(x) => x.toLong }, { case x: Long => JLong(x) }))
  implicit val json4sFormats = DefaultFormats + StringToLong

  trait ActionState[S] {
    val state: S
  }

  trait InitialActionState extends ActionState[Unit] {
    val state = ()
  }
}
