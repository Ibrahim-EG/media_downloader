package dev.echo.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.echo.app.data.parser.LinkParser
import dev.echo.app.data.parser.YtDlpParser
import kotlinx.coroutines.launch

class MainViewModel(
    private val parser: LinkParser = YtDlpParser()
) : ViewModel() {

    var state by mutableStateOf(MainUiState())
        private set

    fun onUrlChange(newUrl: String) {
        state = state.copy(url = newUrl)
    }

    fun parseUrl() {
        val url = state.url.trim()

        if (url.isEmpty()) {
            state = state.copy(error = "Paste a link first.")
            return
        }

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            parser.parse(url)
                .onSuccess { parsed ->
                    state = state.copy(
                        isLoading = false,
                        result = parsed
                    )
                }
                .onFailure { throwable ->
                    state = state.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to parse link."
                    )
                }
        }
    }
}
