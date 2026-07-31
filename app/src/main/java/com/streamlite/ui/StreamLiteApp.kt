package com.streamlite.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamlite.core.AudioSource
import com.streamlite.core.StreamPhase
import com.streamlite.core.StreamStats
import com.streamlite.stream.StreamingService
import java.util.Locale

private val Colors = darkColorScheme(primary = Color(0xFF8AB4F8), secondary = Color(0xFFB8C4FF), surface = Color(0xFF101114), background = Color(0xFF08090B))

@Composable
fun StreamLiteApp(viewModel: StreamViewModel = hiltViewModel()) {
  val savedConfig by viewModel.config.collectAsStateWithLifecycle()
  val stats by viewModel.stats.collectAsStateWithLifecycle()
  val context = LocalContext.current
  var config by remember(savedConfig) { mutableStateOf(savedConfig) }
  var error by remember { mutableStateOf<String?>(null) }
  LaunchedEffect(Unit) { StreamViewModel.errors.collect { error = it } }
  MaterialTheme(colorScheme = Colors) {
    Surface(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("StreamLite", style = MaterialTheme.typography.headlineMedium)
        Selector("Resolution", "${config.width}×${config.height}", listOf("1280×720", "1600×900", "1920×1080")) {
          val parts = it.split("×"); config = config.copy(width = parts[0].toInt(), height = parts[1].toInt())
        }
        Selector("FPS", config.fps.toString(), listOf("30", "60")) { config = config.copy(fps = it.toInt()) }
        Selector("Bitrate (kbps)", config.bitrateKbps.toString(), listOf("6000", "8000", "10000", "12000", "15000", "18000", "20000")) { config = config.copy(bitrateKbps = it.toInt()) }
        Selector("Audio Source", config.audioSource.name.lowercase().replaceFirstChar { it.titlecase() }, listOf("Internal", "Microphone", "Mixed")) { config = config.copy(audioSource = AudioSource.valueOf(it.uppercase())) }
        OutlinedTextField(config.rtmpsUrl, { config = config.copy(rtmpsUrl = it) }, label = { Text("RTMPS URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(config.streamKey, { config = config.copy(streamKey = it) }, label = { Text("Stream Key") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          Button(onClick = { viewModel.requestStart(config) }, enabled = stats.phase !in setOf(StreamPhase.PREPARING, StreamPhase.CONNECTING, StreamPhase.LIVE), modifier = Modifier.weight(1f)) { Text("Start Streaming") }
          TextButton(onClick = { StreamingService.stop(context) }, enabled = stats.phase in setOf(StreamPhase.CONNECTING, StreamPhase.LIVE), modifier = Modifier.weight(1f)) { Text("Stop Streaming") }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        StatusCard(stats)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Selector(
  label: String,
  selected: String,
  options: List<String>,
  select: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
    OutlinedTextField(selected, {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { select(option); expanded = false }) }
    }
  }
}

@Composable
private fun StatusCard(stats: StreamStats) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
      Text("Status: ${stats.message}")
      Text("Dropped Frames: ${stats.droppedFrames}")
      Text("Current Bitrate: ${"%.1f".format(Locale.US, stats.currentBitrateBps / 1_000_000.0)} Mbps")
      Text("Current FPS: ${stats.currentFps}")
      Text("Elapsed Time: %02d:%02d:%02d".format(stats.elapsedSeconds / 3600, (stats.elapsedSeconds % 3600) / 60, stats.elapsedSeconds % 60))
    }
  }
}
