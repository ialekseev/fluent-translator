package com.smartelk.translator.actions

import com.smartelk.translator.Dsl.AudioContentType.AudioContentType
import com.smartelk.translator.Dsl.AudioQuality.AudioQuality
import com.smartelk.translator.Dsl.{scalaFuture, scalazTask}
import com.smartelk.translator.TranslatorSettings
import scala.concurrent.Future
import scalaz.concurrent.Task

private[translator] object SpeakAction {
   case class SpeakRequest(text: String,
                           lang: Option[String] = None,
                           audioContentType: Option[AudioContentType] = None,
                           quality: Option[AudioQuality] = None) {
     require(!text.isEmpty, "Text to be spoken must not be empty")
     require(text.length <= speakTextSizeLimit, s"The size of text to be spoken must not exceed $speakTextSizeLimit characters")
   }

  val speakTextSizeLimit = 2000

   class SpeaksActionTextState(val state: SpeakRequest) extends ActionState[SpeakRequest]{
     def in(lang: String) = {
       require(!lang.isEmpty, "Language to speak IN must not be empty")
       new SpeakActionInState(state.copy(lang = Some(lang)))
     }
   }

   class SpeakActionInState(val state: SpeakRequest) extends ActionState[SpeakRequest]{
     def withAudioContentType(contentType: AudioContentType) = {
       new SpeakActionInState(state.copy(audioContentType = Some(contentType)))
     }

     def withQuality(quality: AudioQuality) = {
       new SpeakActionInState(state.copy(quality = Some(quality)))
     }

     def as(scalazTaskWord: scalazTask.type)(implicit translatorSettings: TranslatorSettings): Task[String] = ???
     def as(scalaFutureWord: scalaFuture.type)(implicit translatorSettings: TranslatorSettings): Future[String] = ???
   }
 }


