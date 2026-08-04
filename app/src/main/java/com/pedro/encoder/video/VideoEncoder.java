/*
 * Copyright (C) 2024 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pedro.encoder.video;

import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.pedro.common.TimeUtils;
import com.pedro.encoder.BaseEncoder;
import com.pedro.encoder.Frame;
import com.pedro.encoder.TimestampMode;
import com.pedro.encoder.input.video.FpsLimiter;
import com.pedro.encoder.input.video.GetCameraData;
import com.pedro.encoder.utils.CodecUtil;
import com.pedro.encoder.utils.SpsColorPatcher;
import com.pedro.encoder.utils.yuv.YUVUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

/**
 * Professional Grade Optimized VideoEncoder for StreamLite.
 * Specifically tuned for iQOO 13 (Snapdragon 8 Elite) and high-motion gaming.
 */
public class VideoEncoder extends BaseEncoder implements GetCameraData {

  private final GetVideoData getVideoData;
  private volatile boolean spsPpsSetted = false;
  private boolean forceKey = false;
  private ByteBuffer oldSps, oldPps, oldVps;
  private Surface inputSurface;

  private int width = 640;
  private int height = 480;
  private int fps = 30;
  private int bitRate = 1200 * 1024;
  private int rotation = 90;
  private int iFrameInterval = 2;
  private long firstTimestamp = 0;
  private final FpsLimiter fpsLimiter = new FpsLimiter();
  private FormatVideoEncoder formatVideoEncoder = FormatVideoEncoder.YUV420Dynamical;
  private int profile = -1;
  private int level = -1;
  private final SpsColorPatcher spsColorPatcher = new SpsColorPatcher();
  private boolean forceBt709Color = false;

  public VideoEncoder(GetVideoData getVideoData) {
    this.getVideoData = getVideoData;
    typeError = CodecUtil.CodecTypeError.VIDEO_CODEC;
    type = CodecUtil.H264_MIME;
    TAG = "VideoEncoder";
  }

  public boolean prepareVideoEncoder(int width, int height, int fps, int bitRate, int rotation,
      int iFrameInterval, FormatVideoEncoder formatVideoEncoder) {
    return prepareVideoEncoder(width, height, fps, bitRate, rotation, iFrameInterval,
        formatVideoEncoder, -1, -1);
  }

  public boolean prepareVideoEncoder(int width, int height, int fps, int bitRate, int rotation,
      int iFrameInterval, FormatVideoEncoder formatVideoEncoder, int profile,
      int level) {
    if (prepared) stop();

    this.width = width;
    this.height = height;
    this.fps = fps;
    this.bitRate = bitRate;
    this.rotation = rotation;
    this.iFrameInterval = iFrameInterval;
    this.formatVideoEncoder = formatVideoEncoder;
    this.profile = profile;
    this.level = level;
    isBufferMode = true;
    
    MediaCodecInfo encoder = chooseEncoder(type);
    try {
      if (encoder == null) return false;
      Log.i(TAG, "Encoder selected: " + encoder.getName());
      codec = MediaCodec.createByCodecName(encoder.getName());
      
      if (this.formatVideoEncoder == FormatVideoEncoder.YUV420Dynamical) {
        this.formatVideoEncoder = chooseColorDynamically(encoder);
      }

      MediaFormat videoFormat;
      if ((rotation == 90 || rotation == 270)) {
        videoFormat = MediaFormat.createVideoFormat(type, height, width);
      } else {
        videoFormat = MediaFormat.createVideoFormat(type, width, height);
      }

      videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, this.formatVideoEncoder.getFormatCodec());
      videoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
      videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
      videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
      
      // OPTIMIZATION: Intra Refresh for smoother bitrate and reduced network jitter
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          videoFormat.setInteger(MediaFormat.KEY_INTRA_REFRESH_PERIOD, fps);
          videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10); 
      } else {
          videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
      }

      // OPTIMIZATION: CBR Mode for stability
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && CodecUtil.isCBRModeSupported(encoder, type)) {
        videoFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
      }

      // PROFILE: High Profile for better detail
      if (this.profile > 0) {
        videoFormat.setInteger(MediaFormat.KEY_PROFILE, this.profile);
      }
      if (this.level > 0) {
        videoFormat.setInteger(MediaFormat.KEY_LEVEL, this.level);
      }

      // COLOR: BT.709 Full Range
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && forceBt709Color) {
        videoFormat.setInteger(MediaFormat.KEY_COLOR_STANDARD, MediaFormat.COLOR_STANDARD_BT709);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_TRANSFER, MediaFormat.COLOR_TRANSFER_SDR_VIDEO);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_FULL);
      }

      // PERFORMANCE: Real-time Priority & Operating Rate
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        videoFormat.setInteger(MediaFormat.KEY_PRIORITY, 0); 
        videoFormat.setInteger(MediaFormat.KEY_OPERATING_RATE, fps);
      }

      // LATENCY: Low Latency Mode (Android 11+)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        videoFormat.setInteger(MediaFormat.KEY_LOW_LATENCY, 1);
      }

      // IMAGE QUALITY: Standard QP Range control (Android 12+)
      // Lower QP means higher quality. Capping Max QP prevents macroblocking in high motion.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          videoFormat.setInteger(MediaFormat.KEY_VIDEO_QP_MIN, 10);
          videoFormat.setInteger(MediaFormat.KEY_VIDEO_QP_MAX, 32); // Slightly more aggressive than 35 for better HUD sharpness
      }

      // QUALCOMM SNAPDRAGON 8 ELITE SPECIFIC OPTIMIZATIONS
      String encoderName = encoder.getName().toLowerCase();
      if (encoderName.contains("qcom") || encoderName.contains("c2.qti")) {
          // Pre-analysis significantly improves motion clarity
          videoFormat.setInteger("vendor.qti-ext-enc-pre-analysis.enable", 1);
          // Complexity 10 for best compression efficiency
          videoFormat.setInteger("vendor.qti-ext-enc-complexity", 10);
          // QP range for Qualcomm
          videoFormat.setString("vendor.qti-ext-enc-qp-range", "10,32,10,32,10,32");
          // Slice pacing for smoother RTMP delivery
          videoFormat.setInteger("vendor.qti-ext-enc-slice-pacing.enable", 1);
          // Specific low latency toggle
          videoFormat.setInteger("vendor.qti-ext-enc-low-latency.enable", 1);
      }

      setCallback();
      codec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
      running = false;
      if (formatVideoEncoder == FormatVideoEncoder.SURFACE) {
        isBufferMode = false;
        inputSurface = codec.createInputSurface();
      }
      prepared = true;
      return true;
    } catch (Exception e) {
      Log.e(TAG, "Encoder initialization failed", e);
      this.stop();
      return false;
    }
  }

  @Override
  public void start(boolean resetTs) {
    if (resetTs) firstTimestamp = 0;
    forceKey = false;
    shouldReset = resetTs;
    spsPpsSetted = false;
    if (formatVideoEncoder != FormatVideoEncoder.SURFACE) {
      YUVUtil.preAllocateBuffers(width * height * 3 / 2);
    }
    Log.i(TAG, "started");
  }

  @Override
  protected void stopImp() {
    spsPpsSetted = false;
    if (inputSurface != null) inputSurface.release();
    inputSurface = null;
    oldSps = null; oldPps = null; oldVps = null;
  }

  @Override
  public boolean reset() {
    stop(false);
    boolean result = prepareVideoEncoder(width, height, fps, bitRate, rotation, iFrameInterval, formatVideoEncoder, profile, level);
    if (!result) return false;
    restart();
    return true;
  }

  private FormatVideoEncoder chooseColorDynamically(MediaCodecInfo mediaCodecInfo) {
    for (int color : mediaCodecInfo.getCapabilitiesForType(type).colorFormats) {
      if (color == FormatVideoEncoder.YUV420PLANAR.getFormatCodec()) return FormatVideoEncoder.YUV420PLANAR;
      else if (color == FormatVideoEncoder.YUV420SEMIPLANAR.getFormatCodec()) return FormatVideoEncoder.YUV420SEMIPLANAR;
    }
    return null;
  }

  public boolean prepareVideoEncoder() {
    return prepareVideoEncoder(width, height, fps, bitRate, rotation, iFrameInterval, formatVideoEncoder, profile, level);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public void setVideoBitrateOnFly(int bitrate) {
    if (isRunning()) {
      Log.i(TAG, "Dynamic Bitrate Update Requested: " + bitrate + " bps");
      this.bitRate = bitrate;
      Bundle bundle = new Bundle();
      bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bitrate);
      try { codec.setParameters(bundle); } catch (IllegalStateException e) { Log.e(TAG, "encoder need be running", e); }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public void requestKeyframe() {
    if (isRunning()) {
      if (spsPpsSetted && oldSps != null) {
        Bundle bundle = new Bundle();
        bundle.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        try {
          codec.setParameters(bundle);
          getVideoData.onVideoInfo(oldSps, oldPps, oldVps);
        } catch (IllegalStateException e) { Log.e(TAG, "encoder need be running", e); }
      } else { spsPpsSetted = false; forceKey = true; }
    }
  }

  public Surface getInputSurface() { return inputSurface; }
  public void setInputSurface(Surface inputSurface) { this.inputSurface = inputSurface; }
  public int getWidth() { return width; }
  public int getHeight() { return height; }
  public int getRotation() { return rotation; }
  public void setFps(int fps) { this.fps = fps; }
  public void setRotation(int rotation) { this.rotation = rotation; }
  public int getFps() { return fps; }
  public int getBitRate() { return bitRate; }
  public void setForceFps(int fps) { fpsLimiter.setFPS(fps); }

  public void forceBt709Color(boolean enabled) {
    if (prepared) throw new IllegalStateException("Already prepared");
    this.forceBt709Color = enabled;
  }

  @Override
  public void inputYUVData(@NonNull Frame frame) {
    if (running && !queue.offer(frame)) { Log.i(TAG, "frame discarded"); }
  }

  private boolean sendSPSandPPS(MediaFormat mediaFormat) {
    if (Objects.equals(type, CodecUtil.AV1_MIME)) {
      ByteBuffer bufferInfo = mediaFormat.getByteBuffer("csd-0");
      if (bufferInfo != null && bufferInfo.remaining() > 4) {
        oldSps = bufferInfo.duplicate();
        getVideoData.onVideoInfo(oldSps, null, null);
        return true;
      }
    } else if (Objects.equals(type, CodecUtil.H265_MIME)) {
      ByteBuffer bufferInfo = mediaFormat.getByteBuffer("csd-0");
      if (bufferInfo != null) {
        List<ByteBuffer> byteBufferList = VideoEncoderHelper.extractVpsSpsPpsFromH265(bufferInfo.duplicate());
        oldSps = forceBt709Color ? spsColorPatcher.patchSpsNalColorToBt709(byteBufferList.get(1), true) : byteBufferList.get(1);
        oldPps = byteBufferList.get(2);
        oldVps = byteBufferList.get(0);
        getVideoData.onVideoInfo(oldSps, oldPps, oldVps);
        return true;
      }
    } else {
      ByteBuffer sps = mediaFormat.getByteBuffer("csd-0");
      ByteBuffer pps = mediaFormat.getByteBuffer("csd-1");
      if (sps != null && pps != null) {
        oldSps = forceBt709Color ? spsColorPatcher.patchSpsNalColorToBt709(sps.duplicate(), false) : sps.duplicate();
        oldPps = pps.duplicate();
        oldVps = null;
        getVideoData.onVideoInfo(oldSps, oldPps, oldVps);
        return true;
      }
    }
    return false;
  }

  @Override
  protected MediaCodecInfo chooseEncoder(String mime) {
    List<MediaCodecInfo> mediaCodecInfoList;
    if (codecType == CodecUtil.CodecType.HARDWARE) mediaCodecInfoList = CodecUtil.getAllHardwareEncoders(mime, true);
    else if (codecType == CodecUtil.CodecType.SOFTWARE) mediaCodecInfoList = CodecUtil.getAllSoftwareEncoders(mime, true);
    else if (codecType == CodecUtil.CodecType.CBR_PRIORITY) mediaCodecInfoList = CodecUtil.getAllEncodersCbrPriority(mime);
    else mediaCodecInfoList = CodecUtil.getAllEncoders(mime, true, true);

    for (MediaCodecInfo mci : mediaCodecInfoList) {
      MediaCodecInfo.CodecCapabilities codecCapabilities = mci.getCapabilitiesForType(mime);
      for (int color : codecCapabilities.colorFormats) {
        if (formatVideoEncoder == FormatVideoEncoder.SURFACE) {
          if (color == FormatVideoEncoder.SURFACE.getFormatCodec()) return mci;
        } else {
          if (color == FormatVideoEncoder.YUV420PLANAR.getFormatCodec() || color == FormatVideoEncoder.YUV420SEMIPLANAR.getFormatCodec()) return mci;
        }
      }
    }
    return null;
  }

  @Override
  protected Frame getInputFrame() throws InterruptedException {
    Frame frame = queue.take();
    if (frame == null) return null;
    if (fpsLimiter.limitFPS()) return getInputFrame();
    byte[] buffer = frame.getBuffer();
    boolean isYV12 = frame.getFormat() == ImageFormat.YV12;
    int orientation = frame.isFlip() ? frame.getOrientation() + 180 : frame.getOrientation();
    if (orientation >= 360) orientation -= 360;
    buffer = isYV12 ? YUVUtil.rotateYV12(buffer, width, height, orientation) : YUVUtil.rotateNV21(buffer, width, height, orientation);
    buffer = isYV12 ? YUVUtil.YV12toYUV420byColor(buffer, width, height, formatVideoEncoder) : YUVUtil.NV21toYUV420byColor(buffer, width, height, formatVideoEncoder);
    frame.setBuffer(buffer);
    return frame;
  }

  @Override
  protected long calculatePts(Frame frame, long presentTimeUs) { return Math.max(0, frame.getTimeStamp() - presentTimeUs); }

  @Override
  public void formatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
    Log.i(TAG, "--- Negotiated MediaCodec Parameters ---");
    Log.i(TAG, "Encoder Name: " + (codec != null ? "Active" : "Unknown"));
    if (mediaFormat.containsKey(MediaFormat.KEY_WIDTH) && mediaFormat.containsKey(MediaFormat.KEY_HEIGHT)) {
        Log.i(TAG, "Resolution: " + mediaFormat.getInteger(MediaFormat.KEY_WIDTH) + "x" + mediaFormat.getInteger(MediaFormat.KEY_HEIGHT));
    }
    if (mediaFormat.containsKey(MediaFormat.KEY_BIT_RATE)) {
        Log.i(TAG, "Bitrate Applied: " + mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE) + " bps");
    }
    if (mediaFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
        Log.i(TAG, "FPS: " + mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE));
    }
    if (mediaFormat.containsKey(MediaFormat.KEY_PROFILE)) {
        Log.i(TAG, "Profile: " + mediaFormat.getInteger(MediaFormat.KEY_PROFILE));
    }
    if (mediaFormat.containsKey(MediaFormat.KEY_LEVEL)) {
        Log.i(TAG, "Level: " + mediaFormat.getInteger(MediaFormat.KEY_LEVEL));
    }
    if (mediaFormat.containsKey(MediaFormat.KEY_BITRATE_MODE)) {
        int mode = mediaFormat.getInteger(MediaFormat.KEY_BITRATE_MODE);
        String modeStr = (mode == 2) ? "CBR" : (mode == 1) ? "VBR" : "CQ";
        Log.i(TAG, "Bitrate Mode: " + modeStr);
    }
    if (mediaFormat.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
        Log.i(TAG, "Color Format: " + mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT));
    }
    Log.i(TAG, "I-frame Interval: " + iFrameInterval);
    Log.i(TAG, "---------------------------------------");

    getVideoData.onVideoFormat(mediaFormat);
    spsPpsSetted = sendSPSandPPS(mediaFormat);
  }

  @Override
  protected void checkBuffer(@NonNull ByteBuffer byteBuffer, @NonNull MediaCodec.BufferInfo bufferInfo) {
    if (forceKey && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { forceKey = false; requestKeyframe(); }
    fixTimeStamp(bufferInfo);
    if (!spsPpsSetted && Objects.equals(type, CodecUtil.H264_MIME)) {
      Pair<ByteBuffer, ByteBuffer> buffers = VideoEncoderHelper.decodeSpsPpsFromBuffer(byteBuffer.duplicate(), bufferInfo.size);
      if (buffers != null) { oldSps = buffers.first; oldPps = buffers.second; oldVps = null; getVideoData.onVideoInfo(oldSps, oldPps, oldVps); spsPpsSetted = true; }
    } else if (!spsPpsSetted && type.equals(CodecUtil.H265_MIME)) {
      List<ByteBuffer> byteBufferList = VideoEncoderHelper.extractVpsSpsPpsFromH265(byteBuffer.duplicate());
      if (byteBufferList.size() == 3) { oldSps = byteBufferList.get(1); oldPps = byteBufferList.get(2); oldVps = byteBufferList.get(0); getVideoData.onVideoInfo(oldSps, oldPps, oldVps); spsPpsSetted = true; }
    } else if (!spsPpsSetted && type.equals(CodecUtil.AV1_MIME)) {
      ByteBuffer obuSequence = VideoEncoderHelper.extractObuSequence(byteBuffer.duplicate(), bufferInfo);
      if (obuSequence != null) { oldSps = obuSequence; getVideoData.onVideoInfo(obuSequence, null, null); spsPpsSetted = true; }
    }
    if (timestampMode == TimestampMode.CLOCK) {
      if (formatVideoEncoder != FormatVideoEncoder.SURFACE) bufferInfo.presentationTimeUs = TimeUtils.getCurrentTimeMicro() - presentTimeUs;
      else { if (firstTimestamp == 0) firstTimestamp = bufferInfo.presentationTimeUs; bufferInfo.presentationTimeUs -= firstTimestamp; }
    } else { if (firstTimestamp == 0) firstTimestamp = bufferInfo.presentationTimeUs; bufferInfo.presentationTimeUs -= firstTimestamp; }
  }

  @Override
  protected void sendBuffer(@NonNull ByteBuffer byteBuffer, @NonNull MediaCodec.BufferInfo bufferInfo) { getVideoData.getVideoData(byteBuffer, bufferInfo); }
}
