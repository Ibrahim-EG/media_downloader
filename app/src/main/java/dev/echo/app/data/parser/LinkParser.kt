package dev.echo.app.data.parser

interface LinkParser {
    suspend fun parse(url: String): Result<ParsedMedia>
}
