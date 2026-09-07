package dev.echo.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.echo.app.data.downloader.DownloadManager
import dev.echo.app.data.parser.LinkParser
import dev.echo.app.data.parser.YtDlpParser
import kotlinx.coroutines.launch

class MainViewModel(
    app: Application,
    private val parser: LinkParser = YtDlpParser()
) : AndroidViewModel(app) {

    private val downloadManager = DownloadManager(app.applicationContext)

    var state by mutableStateOf(MainUiState())
        private set

    fun onUrlChange(newUrl: String) {
        state = state.copy(url = newUrl, error = null, successMessage = null)
    }

    fun parseUrl() {
        val url = state.url.trim()
        if (url.isEmpty()) {
            state = state.copy(error = "Paste a link first.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, successMessage = null)
            parser.parse(url)
                .onSuccess { parsed ->
                    state = state.copy(isLoading = false, result = parsed)
                }
                .onFailure { throwable ->
                    state = state.copy(isLoading = false, error = throwable.message ?: "Failed to parse link.")
                }
        }
    }

    fun downloadAudio() {
        val url = state.url.trim()
        if (url.isEmpty()) return

        viewModelScope.launch {
            state = state.copy(isDownloading = true, error = null, successMessage = null)
            downloadManager.downloadAudioAsMp3(url)
                .onSuccess { file ->
                    val sizeMB = file.length() / 1024.0 / 1024.0
                    state = state.copy(
                        isDownloading = false,
                        successMessage = "Audio downloaded to cache! (${String.format("%.1f", sizeMB)} MB)"
                    )
                }
                .onFailure { throwable ->
                    state = state.copy(
                        isDownloading = false,
                        error = "Download failed: ${throwable.message}"
                    )
                }
        }
    }
}
