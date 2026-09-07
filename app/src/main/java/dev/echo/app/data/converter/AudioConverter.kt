package dev.echo.app.data.converter

import com.yausername.youtubedl_android.FFmpeg
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AudioConverter(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun convertToMp3(
        inputFile: File,
        outputFile: File,
        bitrateKbps: Int = 320
    ): Result<File> = withContext(dispatcher) {
        try {
            val command = arrayOf(
                "-y",
                "-i", inputFile.absolutePath,
                "-vn",
                "-acodec", "libmp3lame",
                "-b:a", "${bitrateKbps}k",
                outputFile.absolutePath
            )

            val response = FFmpeg.getInstance().execute(command)

            if (response.exitValue != 0) {
                return@withContext Result.failure(
                    Exception("FFmpeg failed: ${response.err}")
                )
            }

            Result.success(outputFile)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
