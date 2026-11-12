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
 * and when the phone cannot speak English the user is told so with a toast.
 */
class Speaker(context: Context) {

    private enum class Status { STARTING, READY, UNAVAILABLE }

    private val appContext = context.applicationContext

    // Written on the TTS callback thread, read on the main thread.
    @Volatile private var status = Status.STARTING
    @Volatile private var waiting: String? = null

    private val tts: TextToSpeech = TextToSpeech(appContext) { result ->
        status = if (result == TextToSpeech.SUCCESS && setEnglish()) Status.READY else Status.UNAVAILABLE
        val queued = waiting
        waiting = null
        if (queued != null) say(queued)
    }

    /** False when the engine has no English voice at all. */
    private fun setEnglish(): Boolean {
        val outcome = runCatching { tts.setLanguage(Locale.US) }.getOrDefault(TextToSpeech.LANG_NOT_SUPPORTED)
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
