package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsFeedbackManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                val bnLocale = Locale.forLanguageTag("bn-BD")
                val res = tts?.isLanguageAvailable(bnLocale)
                if (res != null && res >= TextToSpeech.LANG_AVAILABLE) {
                    tts?.language = bnLocale
                } else {
                    tts?.language = Locale.forLanguageTag("bn")
                }
            }
        }
    }

    fun speakBengali(text: String) {
        if (!isReady) return
        val engine = tts ?: return
        val bnLocale = Locale.forLanguageTag("bn-BD")
        if (engine.isLanguageAvailable(bnLocale) >= TextToSpeech.LANG_AVAILABLE) {
            engine.language = bnLocale
        }
        engine.setSpeechRate(0.95f)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bn_${System.currentTimeMillis()}")
    }

    fun speakDuaAudio(arabic: String, transliterationBn: String) {
        if (!isReady) return
        val engine = tts ?: return
        val arLocale = Locale.forLanguageTag("ar")
        if (engine.isLanguageAvailable(arLocale) >= TextToSpeech.LANG_AVAILABLE) {
            engine.language = arLocale
            engine.setSpeechRate(0.82f)
            engine.speak(arabic, TextToSpeech.QUEUE_FLUSH, null, "ar_${System.currentTimeMillis()}")
        } else {
            speakBengali(transliterationBn)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
