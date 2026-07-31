package com.streamlite.core

enum class AudioSource { INTERNAL, MICROPHONE, MIXED }

data class StreamConfig(
  val width: Int = 1920,
  val height: Int = 1080,
  val fps: Int = 60,
  val bitrateKbps: Int = 12_000,
  val audioSource: AudioSource = AudioSource.MIXED,
  val rtmpsUrl: String = "",
  val streamKey: String = ""
) {
  val endpoint: String
    get() = rtmpsUrl.trimEnd('/') + "/" + streamKey.trim().trimStart('/')

  fun validate(): String? = when {
    !rtmpsUrl.trim().startsWith("rtmps://") -> "Enter a valid RTMPS URL."
    streamKey.isBlank() -> "Enter a stream key."
    else -> null
  }
}

data class StreamStats(
  val phase: StreamPhase = StreamPhase.IDLE,
  val message: String = "Ready",
  val currentBitrateBps: Long = 0L,
  val currentFps: Int = 0,
  val droppedFrames: Long = 0L,
  val elapsedSeconds: Long = 0L
)

enum class StreamPhase { IDLE, PREPARING, CONNECTING, LIVE, RECONNECTING, ERROR }
