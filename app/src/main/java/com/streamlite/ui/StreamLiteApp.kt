package com.streamlite.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamlite.core.AudioSource
import com.streamlite.core.StreamConfig
import com.streamlite.core.StreamPhase
import com.streamlite.core.StreamStats
import com.streamlite.stream.StreamingService

private val DarkBackground = Color(0xFF09090B)
private val DarkCard = Color(0xFF18181B)
private val DarkCardBorder = Color(0xFF27272A)
private val PrimaryRed = Color(0xFFFF3B30)
private val SuccessGreen = Color(0xFF22C55E)
private val WarningYellow = Color(0xFFFACC15)
private val TextPrimary = Color(0xFFFAFAFA)
private val TextSecondary = Color(0xFFA1A1AA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamLiteApp(viewModel: StreamViewModel) {
  val savedConfig by viewModel.config.collectAsStateWithLifecycle()
  val stats by viewModel.stats.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  var config by remember(savedConfig) { mutableStateOf(savedConfig) }
  val context = LocalContext.current

  LaunchedEffect(Unit) {
    StreamViewModel.errors.collect { snackbarHostState.showSnackbar(it) }
  }

  val isStreaming = stats.phase == StreamPhase.LIVE || stats.phase == StreamPhase.RECONNECTING
  val isBusy = stats.phase == StreamPhase.PREPARING || stats.phase == StreamPhase.CONNECTING
  val enabled = !isStreaming && !isBusy

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = DarkBackground
  ) { padding ->
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      color = DarkBackground
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
      ) {
      HeaderSection(stats.phase)

      GoLiveButtonSection(
        isStreaming = isStreaming,
        isBusy = isBusy,
        enabled = enabled,
        onStart = { viewModel.requestStart(config) },
        onStop = { StreamingService.stop(context) }
      )

      ConfigSection(
        config = config,
        enabled = enabled,
        onConfigChange = { config = it }
      )

      StatsDashboard(stats = stats)

      UrlKeySection(
        config = config,
        enabled = enabled,
        onConfigChange = { config = it }
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
}

@Composable
private fun HeaderSection(phase: StreamPhase) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        imageVector = Icons.Rounded.Videocam,
        contentDescription = null,
        tint = PrimaryRed,
        modifier = Modifier.size(28.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "StreamLite",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Black,
          letterSpacing = (-0.5).sp
        ),
        color = TextPrimary
      )
    }
    Text(
      text = "Professional Mobile Streaming",
      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(6.dp))

    StatusChip(phase = phase)
  }
}

@Composable
private fun StatusChip(phase: StreamPhase) {
  val (statusText, statusColor) = when (phase) {
    StreamPhase.IDLE -> "READY" to SuccessGreen
    StreamPhase.PREPARING, StreamPhase.CONNECTING -> "CONNECTING" to WarningYellow
    StreamPhase.LIVE -> "LIVE" to PrimaryRed
    StreamPhase.RECONNECTING -> "RECONNECTING" to WarningYellow
    else -> "ERROR" to PrimaryRed
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  Surface(
    color = statusColor.copy(alpha = 0.15f),
    shape = RoundedCornerShape(50),
    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(statusColor.copy(alpha = if (phase == StreamPhase.IDLE) 1f else alpha))
      )
      AnimatedContent(
        targetState = statusText,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "statusText"
      ) { target ->
        Text(
          text = target,
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          ),
          color = statusColor
        )
      }
    }
  }
}

@Composable
private fun GoLiveButtonSection(
  isStreaming: Boolean,
  isBusy: Boolean,
  enabled: Boolean,
  onStart: () -> Unit,
  onStop: () -> Unit
) {
  val buttonColor by animateColorAsState(
    targetValue = when {
      isStreaming -> PrimaryRed
      isBusy -> WarningYellow
      else -> PrimaryRed
    },
    animationSpec = tween(400),
    label = "buttonColor"
  )

  val buttonText = when {
    isStreaming -> "LIVE NOW"
    isBusy -> "CONNECTING..."
    else -> "GO LIVE"
  }

  val buttonIcon = when {
    isStreaming -> Icons.Rounded.RadioButtonChecked
    isBusy -> Icons.Rounded.Sync
    else -> Icons.Rounded.PlayArrow
  }

  val scale by animateFloatAsState(
    targetValue = if (isStreaming) 1.02f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "scale"
  )

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Button(
      onClick = { if (enabled) onStart() },
      enabled = enabled,
      shape = RoundedCornerShape(24.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = buttonColor,
        disabledContainerColor = buttonColor.copy(alpha = 0.6f)
      ),
      elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 8.dp,
        pressedElevation = 2.dp
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .scale(scale)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
      ) {
        Icon(
          imageVector = buttonIcon,
          contentDescription = null,
          tint = if (isBusy) Color.Black else TextPrimary,
          modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        AnimatedContent(
          targetState = buttonText,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "buttonText"
        ) { text ->
          Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            ),
            color = if (isBusy) Color.Black else TextPrimary
          )
        }
      }
    }

    AnimatedVisibility(
      visible = isStreaming || isBusy,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
    ) {
      OutlinedButton(
        onClick = onStop,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, PrimaryRed),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = DarkCard,
          contentColor = PrimaryRed
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Rounded.Stop,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "STOP STREAM",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          )
        }
      }
    }
  }
}

@Composable
private fun ConfigSection(
  config: StreamConfig,
  enabled: Boolean,
  onConfigChange: (StreamConfig) -> Unit
) {
  val optionsWidthHeight = listOf(
    "720P" to Pair(1280, 720),
    "1080P" to Pair(1920, 1080),
    "1440P" to Pair(2560, 1440),
    "4K" to Pair(3840, 2160)
  )
  val optionsFps = listOf(24, 30, 60, 120)
  val optionsBitrate = listOf(4, 8, 12, 18, 20, 30)
  val audioOptions = listOf(
    AudioSource.INTERNAL to "Internal",
    AudioSource.MICROPHONE to "Microphone",
    AudioSource.MIXED to "Mixed"
  )

  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "STREAM SETTINGS",
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      ),
      color = TextSecondary,
      modifier = Modifier.padding(start = 4.dp)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Box(modifier = Modifier.weight(1f)) {
        ConfigCard(
          title = "Resolution",
          value = resolutionLabel(config.width, config.height),
          icon = Icons.Rounded.AspectRatio,
          enabled = enabled,
          options = optionsWidthHeight.map { it.first },
          onSelect = { selected ->
            optionsWidthHeight.firstOrNull { it.first == selected }?.let {
              onConfigChange(config.copy(width = it.second.first, height = it.second.second))
            }
          }
        )
      }
      Box(modifier = Modifier.weight(1f)) {
        ConfigCard(
          title = "FPS",
          value = "${config.fps}",
          icon = Icons.Rounded.Speed,
          enabled = enabled,
          options = optionsFps.map { it.toString() },
          onSelect = { selected ->
            selected.toIntOrNull()?.let { onConfigChange(config.copy(fps = it)) }
          }
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Box(modifier = Modifier.weight(1f)) {
        ConfigCard(
          title = "Bitrate",
          value = "${config.bitrateKbps / 1000} Mbps",
          icon = Icons.Rounded.NetworkCheck,
          enabled = enabled,
          options = optionsBitrate.map { "$it Mbps" },
          onSelect = { selected ->
            selected.replace(" Mbps", "").toIntOrNull()?.let {
              onConfigChange(config.copy(bitrateKbps = it * 1000))
            }
          }
        )
      }
      Box(modifier = Modifier.weight(1f)) {
        ConfigCard(
          title = "Audio Source",
          value = audioOptions.firstOrNull { it.first == config.audioSource }?.second ?: "",
          icon = Icons.Rounded.Mic,
          enabled = enabled,
          options = audioOptions.map { it.second },
          onSelect = { selected ->
            audioOptions.firstOrNull { it.second == selected }?.let {
              onConfigChange(config.copy(audioSource = it.first))
            }
          }
        )
      }
    }
  }
}

@Composable
private fun ConfigCard(
  title: String,
  value: String,
  icon: ImageVector,
  enabled: Boolean,
  options: List<String>,
  onSelect: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
    color = DarkCard,
    shape = RoundedCornerShape(24.dp),
    border = BorderStroke(1.dp, DarkCardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .clickable(enabled = enabled) { expanded = true }
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (enabled) PrimaryRed else TextSecondary,
          modifier = Modifier.size(20.dp)
        )
        Icon(
          imageVector = Icons.Rounded.ArrowDropDown,
          contentDescription = null,
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary
        )
        Text(
          text = value,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = if (enabled) TextPrimary else TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .background(DarkCard)
        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = {
            Text(
              text = option,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimary
            )
          },
          onClick = {
            onSelect(option)
            expanded = false
          }
        )
      }
    }
  }
}

@Composable
private fun StatsDashboard(stats: StreamStats) {
  val items = listOf(
    StatItem("FPS", stats.currentFps.toString(), Icons.Rounded.SlowMotionVideo, PrimaryRed),
    StatItem("Bitrate", "${stats.currentBitrateBps / 1000} kbps", Icons.Rounded.Equalizer, SuccessGreen),
    StatItem("Dropped", stats.droppedFrames.toString(), Icons.Rounded.Warning, WarningYellow),
    StatItem("Uptime", formatElapsedTime(stats.elapsedSeconds), Icons.Rounded.Timer, TextPrimary)
  )

  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "LIVE ANALYTICS",
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      ),
      color = TextSecondary,
      modifier = Modifier.padding(start = 4.dp)
    )

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.height(180.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      userScrollEnabled = false
    ) {
      items(items) { item ->
        Surface(
          color = DarkCard,
          shape = RoundedCornerShape(24.dp),
          border = BorderStroke(1.dp, DarkCardBorder),
          modifier = Modifier.fillMaxSize()
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary
              )
              Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.color,
                modifier = Modifier.size(18.dp)
              )
            }

            AnimatedContent(
              targetState = item.value,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "statValue"
            ) { valueText ->
              Text(
                text = valueText,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}

private data class StatItem(
  val title: String,
  val value: String,
  val icon: ImageVector,
  val color: Color
)

@Composable
private fun UrlKeySection(
  config: StreamConfig,
  enabled: Boolean,
  onConfigChange: (StreamConfig) -> Unit
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "DESTINATION",
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      ),
      color = TextSecondary,
      modifier = Modifier.padding(start = 4.dp)
    )

    PremiumUrlKeyCard(
      title = "RTMPS URL",
      value = config.rtmpsUrl,
      icon = Icons.Rounded.Link,
      enabled = enabled,
      onSave = { onConfigChange(config.copy(rtmpsUrl = it)) }
    )

    PremiumUrlKeyCard(
      title = "Stream Key",
      value = config.streamKey,
      icon = Icons.Rounded.Key,
      enabled = enabled,
      isSecret = true,
      onSave = { onConfigChange(config.copy(streamKey = it)) }
    )
  }
}

@Composable
private fun PremiumUrlKeyCard(
  title: String,
  value: String,
  icon: ImageVector,
  enabled: Boolean,
  isSecret: Boolean = false,
  onSave: (String) -> Unit
) {
  var showDialog by remember { mutableStateOf(false) }

  Surface(
    color = DarkCard,
    shape = RoundedCornerShape(24.dp),
    border = BorderStroke(1.dp, DarkCardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .clickable(enabled = enabled) { showDialog = true }
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(DarkBackground),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = PrimaryRed,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = if (isSecret && value.isNotBlank()) "••••••••••••••••" else value.ifBlank { "Not configured" },
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = if (value.isBlank()) TextSecondary else TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Icon(
        imageVector = Icons.Rounded.Edit,
        contentDescription = null,
        tint = TextSecondary,
        modifier = Modifier.size(18.dp)
      )
    }
  }

  if (showDialog) {
    EditDestinationDialog(
      title = title,
      initialValue = value,
      isSecret = isSecret,
      onDismiss = { showDialog = false },
      onSave = {
        onSave(it)
        showDialog = false
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDestinationDialog(
  title: String,
  initialValue: String,
  isSecret: Boolean,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var text by remember { mutableStateOf(initialValue) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      color = DarkCard,
      shape = RoundedCornerShape(24.dp),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        Text(
          text = "Configure $title",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )

        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          singleLine = true,
          visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
          keyboardOptions = KeyboardOptions(
            keyboardType = if (isSecret) KeyboardType.Password else KeyboardType.Uri
          ),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkBackground,
            unfocusedContainerColor = DarkBackground,
            focusedBorderColor = PrimaryRed,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          TextButton(
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
          ) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = { onSave(text.trim()) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
          ) {
            Text("Save", color = TextPrimary)
          }
        }
      }
    }
  }
}

private fun resolutionLabel(width: Int, height: Int): String {
  return when {
    width >= 3840 && height >= 2160 -> "4K"
    width >= 2560 && height >= 1440 -> "1440P"
    width >= 1920 && height >= 1080 -> "1080P"
    width >= 1280 && height >= 720 -> "720P"
    else -> "${width}x${height}"
  }
}

private fun formatElapsedTime(seconds: Long): String {
  val m = seconds / 60
  val s = seconds % 60
  return "%02d:%02d".format(m, s)
}