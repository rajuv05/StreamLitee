package com.streamlite.stream

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaCodecInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.sources.audio.InternalAudioSource
import com.pedro.encoder.input.sources.audio.MicrophoneSource
import com.pedro.encoder.input.sources.audio.MixAudioSource
import com.pedro.encoder.input.sources.audio.AudioSource as PedroAudioSource
import com.pedro.encoder.input.sources.video.NoVideoSource
import com.pedro.encoder.input.sources.video.ScreenSource
import com.pedro.encoder.utils.CodecUtil
import com.pedro.library.generic.GenericStream
import com.pedro.library.util.BitrateAdapter
import com.streamlite.core.AppLogger
import com.streamlite.core.AudioSource
import com.streamlite.core.StreamConfig
import com.streamlite.core.StreamPhase
import com.streamlite.core.StreamStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StreamingService : Service(), ConnectChecker {
  companion object {
    private const val CHANNEL_ID = "streaming"
    private const val NOTIFICATION_ID = 1001
    private const val ACTION_START = "com.streamlite.action.START"
    private const val ACTION_STOP = "com.streamlite.action.STOP"
    private const val EXTRA_RESULT_CODE = "result_code"
    private const val EXTRA_PROJECTION_DATA = "projection_data"
    private const val EXTRA_WIDTH = "width"
    private const val EXTRA_HEIGHT = "height"
    private const val EXTRA_FPS = "fps"
    private const val EXTRA_BITRATE = "bitrate"
    private const val EXTRA_AUDIO = "audio"
    private const val EXTRA_ENDPOINT = "endpoint"
    private val mutableStats = MutableStateFlow(StreamStats())
    val stats: StateFlow<StreamStats> = mutableStats.asStateFlow()

    fun start(context: Context, resultCode: Int, projectionData: Intent, config: StreamConfig) {
      AppLogger.info("Starting StreamingService with config: $config")
      val intent = Intent(context, StreamingService::class.java).apply {
        action = ACTION_START
        putExtra(EXTRA_RESULT_CODE, resultCode)
        putExtra(EXTRA_PROJECTION_DATA, projectionData)
        putExtra(EXTRA_WIDTH, config.width)
        putExtra(EXTRA_HEIGHT, config.height)
        putExtra(EXTRA_FPS, config.fps)
        putExtra(EXTRA_BITRATE, config.bitrateKbps)
        putExtra(EXTRA_AUDIO, config.audioSource.name)
        putExtra(EXTRA_ENDPOINT, config.endpoint)
      }
      ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
      context.startService(Intent(context, StreamingService::class.java).setAction(ACTION_STOP))
    }
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private lateinit var projectionManager: MediaProjectionManager
  private var projection: MediaProjection? = null
  private var stream: GenericStream? = null
  private var ticker: Job? = null
  private var startedAt = 0L
  private var selectedFps = 60

  override fun onCreate() {
    super.onCreate()
    projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    createChannel()
    AppLogger.info("Starting foreground service")
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = when (intent?.action) {
    ACTION_START -> {
      begin(intent)
      START_NOT_STICKY
    }
    ACTION_STOP -> {
      stopStreaming("Stopped")
      START_NOT_STICKY
    }
    else -> START_NOT_STICKY
  }

  private fun begin(intent: Intent) {
    if (stream?.isStreaming == true) return
    val projectionData = intent.getParcelableExtraCompat<Intent>(EXTRA_PROJECTION_DATA)
    if (projectionData == null) {
      fail("Permission denied: screen-capture authorization was not provided.")
      return
    }
    val endpoint = intent.getStringExtra(EXTRA_ENDPOINT).orEmpty()
    if (!endpoint.startsWith("rtmps://")) {
      fail("Enter a valid RTMPS URL and stream key.")
      return
    }
    startForegroundNow()
    mutableStats.value = StreamStats(phase = StreamPhase.PREPARING, message = "Preparing encoder")
    serviceScope.launch {
      try {
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        AppLogger.info("Obtaining MediaProjection")
        val mediaProjection = projectionManager.getMediaProjection(resultCode, projectionData)
          ?: error("MediaProjection was not granted")
        projection = mediaProjection
        val width = intent.getIntExtra(EXTRA_WIDTH, 1920)
        val height = intent.getIntExtra(EXTRA_HEIGHT, 1080)
        selectedFps = intent.getIntExtra(EXTRA_FPS, 60)
        val bitrate = intent.getIntExtra(EXTRA_BITRATE, 12_000) * 1_000
        val audio = runCatching { AudioSource.valueOf(intent.getStringExtra(EXTRA_AUDIO).orEmpty()) }.getOrDefault(AudioSource.MIXED)
        AppLogger.info("Creating hardware H.264 encoder ${width}x${height}@${selectedFps} ${bitrate}bps")
        val audioSource = createAudioSource(audio, mediaProjection)
        val generic = GenericStream(applicationContext, this@StreamingService, NoVideoSource(), audioSource)
        
        // Force professional BT.709 color standard for accurate gaming colors.
        generic.forceBt709Color(true)
        
        // Optimization 3: Force Hardware CBR Priority to ensure the most stable bitrate mode is selected.
        // We use FIRST_COMPATIBLE_FOUND for audio to avoid the "AAC encoder unavailable" regression.
        generic.forceCodecType(
          CodecUtil.CodecType.CBR_PRIORITY,
          CodecUtil.CodecType.FIRST_COMPATIBLE_FOUND,
        )
        
        generic.getGlInterface().setForceRender(true, selectedFps)
        generic.setFpsListener { fps -> onFps(fps) }
        
        // Optimization 2: Use AVC High Profile and Level 5.2 to leverage Adreno VPU capabilities.
        // High Profile provides better compression efficiency and stability for 1080p60 on Snapdragon 8 Elite.
        val profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh
        val level = MediaCodecInfo.CodecProfileLevel.AVCLevel52
        
        check(generic.prepareVideo(width, height, bitrate, selectedFps, 2, 0, profile, level)) { 
            "Hardware H.264 High Profile encoder unavailable for this configuration."
        }
        
        check(generic.prepareAudio(48_000, true, 128_000, echoCanceler = audio != AudioSource.INTERNAL, noiseSuppressor = audio != AudioSource.INTERNAL)) {
          "AAC audio encoder or selected audio source is unavailable."
        }
        
        // Optimization 1: Increase network packet cache to 1024 (from default 400).
        // This provides a larger safety margin for high-bitrate data during minor network jitters.
        generic.getStreamClient().resizeCache(1024)
        // Increase RTMP chunk size to 4096 for better transmission efficiency.
        generic.getStreamClient().setWriteChunkSize(4096)
        // Smooth out bitrate adjustments to prevent aggressive oscillations.
        generic.getStreamClient().setBitrateExponentialFactor(0.2f)
        
        // Initialize slow-acting Bitrate Adapter (5% increments/decrements)
        // BUG FIX: The previous logic was capping bitrate during low-motion scenes.
        // NEW LOGIC: Only decrease on congestion; recover to MAX when clear.
        var currentTargetBitrate = bitrate
        val bitrateAdapter = BitrateAdapter { adaptedBitrate ->
            if (generic.isStreaming && (adaptedBitrate != currentTargetBitrate)) {
                currentTargetBitrate = adaptedBitrate
                AppLogger.info("Adapting bitrate to: $adaptedBitrate bps")
                generic.setVideoBitrateOnFly(adaptedBitrate)
            }
        }.apply {
            setMaxBitrate(bitrate)
            setDecreaseRange(5f) // 5% decrease per interval on congestion
            setIncreaseRange(10f) // 10% recovery per interval when clear
        }

        AppLogger.info("--- Encoder Initialization Request ---")
        AppLogger.info("Resolution: ${width}x${height}")
        AppLogger.info("FPS: $selectedFps")
        AppLogger.info("Requested Bitrate: $bitrate bps")
        AppLogger.info("---------------------------------------")

        AppLogger.info("Creating MediaProjection virtual display")
        generic.changeVideoSource(ScreenSource(applicationContext, mediaProjection))
        stream = generic
        startedAt = System.currentTimeMillis()
        ticker?.cancel()
        ticker = serviceScope.launch {
          while (stream?.isStreaming == true) {
            delay(1_000)
            val currentStats = mutableStats.value
            mutableStats.value = currentStats.copy(elapsedSeconds = (System.currentTimeMillis() - startedAt) / 1_000)
            
            // Perform slow adaptive bitrate adjustment based on congestion
            val hasCongestion = generic.getStreamClient().hasCongestion(20f)
            // If no congestion, we want to aim for MAX bitrate, not current output bitrate.
            val inputBitrateForAdapter = if (hasCongestion) currentStats.currentBitrateBps else bitrate.toLong()
            bitrateAdapter.adaptBitrate(inputBitrateForAdapter, hasCongestion)
          }
        }
        AppLogger.info("Connecting RTMPS")
        mutableStats.value = mutableStats.value.copy(phase = StreamPhase.CONNECTING, message = "Connecting")
        generic.startStream(endpoint)
      } catch (t: Throwable) {
        AppLogger.error("Streaming start failed", t)
        fail(readableMessage(t))
      }
    }
  }

  private fun createAudioSource(type: AudioSource, mediaProjection: MediaProjection): PedroAudioSource = when (type) {
    AudioSource.INTERNAL -> InternalAudioSource(mediaProjection)
    AudioSource.MICROPHONE -> MicrophoneSource()
    AudioSource.MIXED -> MixAudioSource(mediaProjection)
  }

  private fun onFps(fps: Int) {
    val prior = mutableStats.value
    mutableStats.value = prior.copy(currentFps = fps, droppedFrames = (selectedFps - fps).coerceAtLeast(0).toLong() + prior.droppedFrames)
  }

  override fun onConnectionStarted(url: String) = AppLogger.info("RTMPS connection started")
  override fun onConnectionSuccess() {
    AppLogger.info("Streaming started")
    mutableStats.value = mutableStats.value.copy(phase = StreamPhase.LIVE, message = "Live")
  }
  override fun onNewBitrate(bitrate: Long) {
    mutableStats.value = mutableStats.value.copy(currentBitrateBps = bitrate)
  }
  override fun onConnectionFailed(reason: String) {
    AppLogger.error("Connection failed: $reason")
    fail("Connection failed: $reason")
  }
  override fun onDisconnect() {
    AppLogger.info("Streaming stopped")
    stopStreaming("Disconnected")
  }
  override fun onAuthError() {
    AppLogger.error("Authentication failed")
    fail("Authentication failed. Check the stream key.")
  }
  override fun onAuthSuccess() = AppLogger.info("RTMPS authentication succeeded")

  private fun stopStreaming(message: String) {
    ticker?.cancel()
    ticker = null
    try {
      if (stream?.isStreaming == true) stream?.stopStream()
      stream?.release()
    } catch (t: Throwable) {
      AppLogger.error("Error while stopping stream", t)
    } finally {
      stream = null
      projection?.stop()
      projection = null
      mutableStats.value = StreamStats(message = message)
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
      AppLogger.info("Streaming service stopped")
    }
  }

  private fun fail(message: String) {
    AppLogger.error(message)
    mutableStats.value = StreamStats(phase = StreamPhase.ERROR, message = message)
    stopStreaming(message)
  }

  private fun readableMessage(t: Throwable): String = when {
    t.message?.contains("encoder", true) == true -> "Encoder unavailable: ${t.message}"
    t.message?.contains("audio", true) == true -> "Audio capture unavailable: ${t.message}"
    else -> t.message ?: "Streaming could not be started."
  }

  private fun createChannel() {
    val manager = getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Live streaming", NotificationManager.IMPORTANCE_LOW))
  }

  private fun startForegroundNow() {
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_sys_upload)
      .setContentTitle("StreamLite is streaming")
      .setContentText("Screen and audio capture are active")
      .setOngoing(true)
      .build()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
    } else startForeground(NOTIFICATION_ID, notification)
  }

  override fun onDestroy() {
    stopStreaming("Stopped")
    serviceScope.coroutineContext[Job]?.cancel()
    super.onDestroy()
  }
  override fun onBind(intent: Intent?): IBinder? = null
}

@Suppress("DEPRECATION")
private inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? = if (Build.VERSION.SDK_INT >= 33) {
  getParcelableExtra(key, T::class.java)
} else getParcelableExtra(key) as? T
