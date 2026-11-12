package com.hueno.coloridentifier.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Reads color names aloud with the system text-to-speech engine. */
class Speaker(context: Context) {

    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) engine()?.language = Locale.US
    }

    private fun engine(): TextToSpeech? = tts

    /** Speaks [text], replacing anything still being spoken. Does nothing until the engine is ready. */
    fun say(text: String) {
        if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hueno-color")
    }

    fun release() {
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
