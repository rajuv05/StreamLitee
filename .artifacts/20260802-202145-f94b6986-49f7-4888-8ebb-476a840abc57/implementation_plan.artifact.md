# Streaming Optimization for iQOO 13 (Snapdragon 8 Elite)

This plan optimizes the streaming engine for the iQOO 13 by tuning the hardware encoder and network buffering for maximum stability and performance on Android 15/16.

## Proposed Changes

### Core Streaming Engine

#### [StreamingService.kt](file:///C:/Users/rajuv/OneDrive/Documents/StreamChamp Clone For Android/app/src/main/java/com/streamlite/stream/StreamingService.kt)

- **Force CBR Bitrate Mode**: Switch from the default bitrate mode (often VBR) to `BitrateMode.CBR`. This prevents aggressive bitrate fluctuations and ensures a stable stream on high-performance hardware like the Snapdragon 8 Elite.
- **Enable High Profile Encoding**: Upgrade the H.264 encoder profile from `Baseline`/`Main` to `High Profile` (AVCProfileHigh) and `Level 5.2`. This leverages the Adreno VPU's capabilities for better compression efficiency and quality.
- **Increase Network Buffers**: Increase the RTMP write buffer to 1MB. This provides a safety margin for 1080p60 streams, preventing frame drops during minor network jitters.
- **Real-time Priority Hinting**: (Where library permits) Hint the system that the encoder should be treated as a real-time process.

```kotlin
// Example of planned changes in StreamingService.kt
val generic = GenericStream(applicationContext, this@StreamingService, NoVideoSource(), audioSource)
// Force Constant Bitrate Mode for stability on iQOO 13
generic.videoEncoder.bitrateMode = BitrateMode.CBR
// Use High Profile (Level 5.2) for Snapdragon 8 Elite optimizations
generic.prepareVideo(width, height, bitrate, selectedFps, 2, 0,
    MediaCodecInfo.CodecProfileLevel.AVCProfileHigh,
    MediaCodecInfo.CodecProfileLevel.AVCLevel52)
// Optimize network buffering for 1080p60
generic.getStreamClient().setWriteBufferSize(1024 * 1024)
```

## Verification Plan

### Automated Tests
- I will perform a static analysis check using `analyze_file` to ensure all library APIs (BitrateMode, etc.) are correctly referenced.
- I will check for any lint warnings related to MediaCodec usage on Android 15/16.

### Manual Verification
- **Bitrate Stability**: Monitor `onNewBitrate` logs (if available) to ensure the bitrate remains close to the target.
- **Performance**: Verify that `onFps` reports stable 60 FPS without significant drops.
- **Hardware Compatibility**: Ensure the `check(generic.prepareVideo(...))` block passes, confirming the hardware encoder accepted the High Profile configuration.
