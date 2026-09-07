package dev.echo.app.ui

import dev.echo.app.data.parser.ParsedMedia

data class MainUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val result: ParsedMedia? = null
)
