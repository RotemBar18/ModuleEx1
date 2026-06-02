package com.example.commonlibrary.utils

import android.content.Context
import android.media.MediaPlayer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SingleSoundPlayer (context: Context) {
    private val context : Context = context.applicationContext
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun playSound(resourceId: Int) {
        executor.execute {
            val mediaPlayer = MediaPlayer.create(
                context,
                resourceId
            )
            mediaPlayer.isLooping = false
            mediaPlayer.setVolume(1.0f,1.0f)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                mp: MediaPlayer? ->
                    var mpl = mp
                mpl?.stop()
                mpl?.release()
                mpl = null
            }
        }
    }

    fun release() {
        executor.shutdown()
    }


}