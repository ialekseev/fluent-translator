package com.smartelk.translator

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import com.smartelk.translator.Dsl._

class DslSpec extends WordSpecLike with Matchers with MockitoSugar {

  implicit val settings = new TranslatorSettings("my-id", "my-secret")

  "Translating" when {

    "text to be translated has too big size" should {
      "throw" in {
        //act
        Translator translate "blabla" from autodetect to "ger" asTask()

        //assert
        //todo: complete
      }
    }
  }
}
