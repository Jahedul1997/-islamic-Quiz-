package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

class SoundEffectPlayer(context: Context) {
    private val appContext = context.applicationContext

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val applauseId = soundPool.load(appContext, R.raw.applause, 1)
    private val wrongSadId = soundPool.load(appContext, R.raw.wrong_sad, 1)
    private val tasbihTickId = soundPool.load(appContext, R.raw.tasbih_tick, 1)
    private val tasbihMilestoneId = soundPool.load(appContext, R.raw.tasbih_milestone, 1)

    fun playApplause() {
        soundPool.play(applauseId, 0.95f, 0.95f, 1, 0, 1f)
    }

    fun playWrongSad() {
        soundPool.play(wrongSadId, 0.9f, 0.9f, 1, 0, 1f)
    }

    fun playTasbihTick() {
        soundPool.play(tasbihTickId, 0.8f, 0.8f, 1, 0, 1f)
    }

    fun playTasbihMilestone() {
        soundPool.play(tasbihMilestoneId, 0.95f, 0.95f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
