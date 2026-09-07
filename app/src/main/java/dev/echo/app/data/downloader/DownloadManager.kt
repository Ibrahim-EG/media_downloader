package dev.echo.app.data.downloader

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DownloadManager(private val context: Context) {

    suspend fun downloadAudioAsMp3(url: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            // We save to the app's private cache first to test the engine safely
            val outputFile = File(context.cacheDir, "echo_downloaded_audio.mp3")
            if (outputFile.exists()) outputFile.delete()

            val request = YoutubeDLRequest(url).apply {
                addOption("-f", "bestaudio")       // Get the best available audio
                addOption("-x")                    // Extract audio from video
                addOption("--audio-format", "mp3") // Convert to MP3
                addOption("--audio-quality", "0")  // 0 = Best quality (320kbps)
                addOption("-o", outputFile.absolutePath)
            }

            val response = YoutubeDL.getInstance().execute(request)

            if (response.exitCode != 0) {
                Result.failure(Exception("Download failed: ${response.err}"))
            } else {
                Result.success(outputFile)
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
