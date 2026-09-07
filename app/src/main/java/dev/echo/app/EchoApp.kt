package dev.echo.app

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL

class EchoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeDownloadEngine()
    }

    private fun initializeDownloadEngine() {
        try {
            YoutubeDL.getInstance().init(this)
            Log.d("EchoApp", "YouTube-DL initialized")
        } catch (t: Throwable) {
            Log.e("EchoApp", "Failed to initialize YouTube-DL", t)
        }

        try {
            FFmpeg.getInstance().init(this)
            Log.d("EchoApp", "FFmpeg initialized")
        } catch (t: Throwable) {
            Log.e("EchoApp", "Failed to initialize FFmpeg", t)
        }
    }
}
