package com.smartelk.translator.actions

import com.smartelk.translator.Dsl._
import com.smartelk.translator.remote.RemoteService.RemoteServiceClient
import scala.concurrent.Future

private[translator] object SpeakAction {
   case class SpeakRequest(text: String,
                           lang: Option[String] = None,
                           audioContentType: Option[AudioContentType] = None,
                           quality: Option[AudioQuality] = None) {
     require(!text.isEmpty, "Text to be spoken must not be empty")
     require(text.length <= speakTextSizeLimit, s"The size of text to be spoken must not exceed $speakTextSizeLimit characters")
   }

  val speakTextSizeLimit = 2000

   class SpeaksActionState(val state: SpeakRequest) extends ActionState[SpeakRequest]{
     def in(lang: String) = {
       require(!lang.isEmpty, "Language to speak IN must not be empty")
       new SpeakActionStateIn(state.copy(lang = Some(lang)))
     }
   }

   class SpeakActionStateIn(val state: SpeakRequest) extends ActionState[SpeakRequest]{
     def withAudioContentType(contentType: AudioContentType) = {
       new SpeakActionStateIn(state.copy(audioContentType = Some(contentType)))
     }

     def withQuality(quality: AudioQuality) = {
       new SpeakActionStateIn(state.copy(quality = Some(quality)))
     }

     def as(scalaFutureWord: future.type)(implicit client: RemoteServiceClient): Future[String] = ???
   }
 }


