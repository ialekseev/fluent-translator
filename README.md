# fluent-translator

Scala library for working with Microsoft, Google(?)... translators in a non-blocking way with a fancy DSL.

Examples
--------------
Using Microsoft translator client:
<pre>
  import com.smartelk.fluent.translator.Dsl._

  implicit val client = new MicrosoftTranslatorClient {
    val clientId = "microsoft client id"
    val clientSecret = "microsoft client secret"
  }

  val res0: Future[String] = Microsoft give me a translation of "How are you?" from "en" to "ru" as future
  val res1: Future[String] = Microsoft give me a translation of "What a lovely weather today!" from "en" to "fr" withContentType `text/html` as future
  val res2: Future[GetTranslationsResponse] = Microsoft give me many translations of "Doing well by doing good" from "fr" to "en" as future
  val res3: Future[GetTranslationsResponse] = Microsoft give me many translations of "Paris holidays" from "en" to "ru" withCategory "general" as future
  val res4: Future[SpeakResponse] = Microsoft speak "I'm doing well enough now" in "en" withAudioContentType `audio/mp3` as future
  val res5: Future[SpeakResponse] = Microsoft speak "How are you doing?" in "en" withQuality MinSize as future
</pre>

Work in progress... gonna be done soon.