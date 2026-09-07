package dev.echo.app.data.parser

import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class YtDlpParser(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LinkParser {

    override suspend fun parse(url: String): Result<ParsedMedia> = withContext(dispatcher) {
        try {
            val request = YoutubeDLRequest(url).apply {
                setOption("--no-playlist")
                setOption("--dump-json")
                setOption("--no-warnings")
                setOption("--skip-download")
            }

            val response = YoutubeDL.getInstance().execute(request)

            if (response.exitCode != 0) {
                return@withContext Result.failure(
                    Exception("Parsing failed: ${response.err}")
                )
            }

            val jsonText = response.out
                .lineSequence()
                .firstOrNull { it.trim().startsWith("{") }
                ?: response.out

            val json = JSONObject(jsonText)
            val parsed = mapJsonToParsedMedia(url, json)

            Result.success(parsed)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private fun mapJsonToParsedMedia(
        url: String,
        json: JSONObject
    ): ParsedMedia {
        val title = json.optString("title", "Unknown title")
        val uploader = json.optString("uploader")
            .takeIf { it.isNotBlank() }
            ?: json.optString("channel").takeIf { it.isNotBlank() }

        val duration = json.optLong("duration", 0L)

        val formatsArray: JSONArray = json.optJSONArray("formats") ?: JSONArray()

        val audioOptions = mutableListOf<AudioOption>()
        val videoOptions = mutableListOf<VideoOption>()

        for (i in 0 until formatsArray.length()) {
            val format = formatsArray.optJSONObject(i) ?: continue

            val formatId = format.optString("format_id", "")
            val ext = format.optString("ext", "")
            val vcodec = format.optString("vcodec", "none")
            val acodec = format.optString("acodec", "none")
            val protocol = format.optString("protocol").takeIf { it.isNotBlank() }
            val streamUrl = format.optString("url").takeIf { it.isNotBlank() }

            val fileSize = format.optLong("filesize", -1L)
                .takeIf { it > 0 }
                ?: format.optLong("filesize_approx", -1L).takeIf { it > 0 }

            val bitrate = when {
                format.optDouble("abr", -1.0) > 0 -> format.optDouble("abr").toInt()
                format.optDouble("tbr", -1.0) > 0 -> format.optDouble("tbr").toInt()
                else -> null
            }

            val height = if (format.has("height")) format.optInt("height") else null
            val fps = if (format.has("fps")) format.optInt("fps") else null

            val hasVideo = !vcodec.equals("none", ignoreCase = true)
            val hasAudio = !acodec.equals("none", ignoreCase = true)

            if (!hasVideo && hasAudio) {
                audioOptions.add(
                    AudioOption(
                        formatId = formatId,
                        ext = ext,
                        bitrateKbps = bitrate,
                        fileSizeBytes = fileSize,
                        protocol = protocol,
                        url = streamUrl
                    )
                )
            } else if (hasVideo) {
                videoOptions.add(
                    VideoOption(
                        formatId = formatId,
                        ext = ext,
                        height = height,
                        fps = fps,
                        bitrateKbps = bitrate,
                        fileSizeBytes = fileSize,
                        protocol = protocol,
                        url = streamUrl
                    )
                )
            }
        }

        return ParsedMedia(
            sourceUrl = url,
            platform = detectPlatform(url),
            title = title,
            uploader = uploader,
            durationSeconds = duration,
            audioOptions = audioOptions.sortedByDescending { it.bitrateKbps ?: 0 },
            videoOptions = videoOptions.sortedByDescending { it.height ?: 0 },
            rawJson = json.toString(2)
        )
    }

    private fun detectPlatform(url: String): Platform {
        val normalized = url.lowercase()

        return when {
            normalized.contains("youtube.com") || normalized.contains("youtu.be") -> Platform.YOUTUBE
            normalized.contains("facebook.com") || normalized.contains("fb.watch") -> Platform.FACEBOOK
            normalized.contains("instagram.com") -> Platform.INSTAGRAM
            else -> Platform.UNKNOWN
        }
    }
}
