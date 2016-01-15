package com.smartelk.fluent.translator.microsoft.actions

import com.smartelk.fluent.translator.Dsl.Microsoft.TranslatorClient
import com.smartelk.fluent.translator.Dsl._
import com.smartelk.fluent.translator.basic.ActionState
import com.smartelk.fluent.translator.microsoft.remote.MicrosoftRemoteServiceClient
import MicrosoftRemoteServiceClient.{SpeakResponse, SpeakRequest}
import scala.concurrent.Future

private[translator] object MicrosoftSpeakAction {

  case class SpeakActionParams(text: String,
                           lang: Option[String] = None,
                           audioContentType: Option[AudioContentType] = None,
                           quality: Option[AudioQuality] = None) {
     require(!text.isEmpty, "Text to be spoken must not be empty")
     require(text.length <= speakTextSizeLimit, s"The size of text to be spoken must not exceed $speakTextSizeLimit characters")
   }

  val speakTextSizeLimit = 2000

   class SpeaksActionState(val state: SpeakActionParams) extends ActionState[SpeakActionParams]{
     def in(lang: String) = {
       require(!lang.isEmpty, "Language to speak IN must not be empty")
       new SpeakActionStateIn(state.copy(lang = Some(lang)))
     }
   }

   class SpeakActionStateIn(val state: SpeakActionParams) extends ActionState[SpeakActionParams]{
     def withAudioContentType(contentType: AudioContentType) = {
       new SpeakActionStateIn(state.copy(audioContentType = Some(contentType)))
     }

     def withQuality(quality: AudioQuality) = {
       new SpeakActionStateIn(state.copy(quality = Some(quality)))
     }

     def as(scalaFutureWord: future.type)(implicit client: TranslatorClient): Future[SpeakResponse] = {
       client.remoteServiceClient.speak(SpeakRequest(state.text, state.lang.get, state.audioContentType, state.quality))
     }
   }
 }


