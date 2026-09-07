package dev.echo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.echo.app.ui.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("ECHO")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.url,
                onValueChange = viewModel::onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Paste YouTube link") },
                singleLine = true
            )

            Row {
                Button(
                    onClick = { viewModel.parseUrl() },
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Parse Link")
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.result?.let { media ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = media.uploader ?: "Unknown uploader",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Platform: ${media.platform.name}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Audio options found: ${media.audioOptions.size}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Video options found: ${media.videoOptions.size}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        media.bestAudio?.let { audio ->
                            Text(
                                text = "Best audio: ${audio.bitrateKbps ?: "?"} kbps • ${audio.ext.uppercase()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        media.bestVideo?.let { video ->
                            Text(
                                text = "Best video: ${video.height ?: "?"}p • ${video.ext.uppercase()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
