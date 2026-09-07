package dev.echo.app.data.parser

enum class Platform {
    YOUTUBE,
    FACEBOOK,
    INSTAGRAM,
    UNKNOWN
}

data class ParsedMedia(
    val sourceUrl: String,
    val platform: Platform,
    val title: String,
    val uploader: String?,
    val durationSeconds: Long,
    val audioOptions: List<AudioOption>,
    val videoOptions: List<VideoOption>,
    val rawJson: String
) {
    val bestAudio: AudioOption?
        get() = audioOptions.maxByOrNull { it.bitrateKbps ?: 0 }

    val bestVideo: VideoOption?
        get() = videoOptions.maxByOrNull { it.height ?: 0 }
}

data class AudioOption(
    val formatId: String,
    val ext: String,
    val bitrateKbps: Int?,
    val fileSizeBytes: Long?,
    val protocol: String?,
    val url: String?
)

data class VideoOption(
    val formatId: String,
    val ext: String,
    val height: Int?,
    val fps: Int?,
    val bitrateKbps: Int?,
    val fileSizeBytes: Long?,
    val protocol: String?,
    val url: String?
)
