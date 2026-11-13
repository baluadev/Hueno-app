package com.hueno.coloridentifier.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.hueno.coloridentifier.R
import java.util.Locale

/**
 * Reads color names aloud with the system text-to-speech engine.
 *
 * Never silent without a reason: text asked for before the engine is ready is spoken once it is,
 * and when the phone cannot speak the app language the user is told so with a toast. Speaks in the
 * app language (taken from the activity context, which follows the in-app language choice), and
 * falls back to English when the engine has no voice for it.
 */
class Speaker(context: Context) {

    private enum class Status { STARTING, READY, UNAVAILABLE }

    private val appContext = context.applicationContext
    private val locale: Locale = context.resources.configuration.locales[0] ?: Locale.US

    // Written on the TTS callback thread, read on the main thread.
    @Volatile private var status = Status.STARTING
    @Volatile private var waiting: String? = null

    private val tts: TextToSpeech = TextToSpeech(appContext) { result ->
        status = if (result == TextToSpeech.SUCCESS && (setLanguage(locale) || setLanguage(Locale.US))) Status.READY else Status.UNAVAILABLE
        val queued = waiting
        waiting = null
        if (queued != null) say(queued)
    }

    /** False when the engine has no voice at all for [language]. */
    private fun setLanguage(language: Locale): Boolean {
        val outcome = runCatching { tts.setLanguage(language) }.getOrDefault(TextToSpeech.LANG_NOT_SUPPORTED)
        return outcome != TextToSpeech.LANG_MISSING_DATA && outcome != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /** Speaks [text], replacing anything still being spoken. */
    fun say(text: String) {
        when (status) {
            // Only the latest request matters; the engine usually starts within a second.
            Status.STARTING -> waiting = text
            Status.READY -> if (tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hueno-color") != TextToSpeech.SUCCESS) {
                tellUnavailable()
            }
            Status.UNAVAILABLE -> tellUnavailable()
        }
    }

    private fun tellUnavailable() {
        // The init callback may arrive off the main thread; toasts need a looper.
        ContextCompat.getMainExecutor(appContext).execute {
            Toast.makeText(appContext, R.string.speak_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    fun release() {
        waiting = null
        tts.stop()
        tts.shutdown()
    }
}

@Composable
fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val speaker = remember { Speaker(context) }
    DisposableEffect(Unit) { onDispose { speaker.release() } }
    return speaker
}
