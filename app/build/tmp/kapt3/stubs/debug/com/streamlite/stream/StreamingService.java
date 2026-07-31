package com.streamlite.stream;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.pedro.common.ConnectChecker;
import com.pedro.encoder.input.sources.audio.InternalAudioSource;
import com.pedro.encoder.input.sources.audio.MicrophoneSource;
import com.pedro.encoder.input.sources.audio.MixAudioSource;
import com.pedro.encoder.input.sources.video.NoVideoSource;
import com.pedro.encoder.input.sources.video.ScreenSource;
import com.pedro.encoder.utils.CodecUtil;
import com.pedro.library.generic.GenericStream;
import com.streamlite.core.AppLogger;
import com.streamlite.core.AudioSource;
import com.streamlite.core.StreamConfig;
import com.streamlite.core.StreamPhase;
import com.streamlite.core.StreamStats;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000p\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0010\u0003\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 :2\u00020\u00012\u00020\u0002:\u0001:B\u0007\u00a2\u0006\u0004\b\u0003\u0010\u0004J\b\u0010\u0013\u001a\u00020\u0014H\u0016J\"\u0010\u0015\u001a\u00020\u00122\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u0018\u001a\u00020\u00122\u0006\u0010\u0019\u001a\u00020\u0012H\u0016J\u0010\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\nH\u0002J\u0010\u0010 \u001a\u00020\u00142\u0006\u0010!\u001a\u00020\u0012H\u0002J\u0015\u0010\"\u001a\u00020\u00122\u0006\u0010#\u001a\u00020$H\u0016\u00a2\u0006\u0002\u0010%J\b\u0010&\u001a\u00020\u0014H\u0016J\u0010\u0010\'\u001a\u00020\u00142\u0006\u0010(\u001a\u00020\u0010H\u0016J\u0010\u0010)\u001a\u00020\u00142\u0006\u0010*\u001a\u00020$H\u0016J\b\u0010+\u001a\u00020\u0014H\u0016J\b\u0010,\u001a\u00020\u0014H\u0016J\r\u0010-\u001a\u00020\u0012H\u0016\u00a2\u0006\u0002\u0010.J\u0010\u0010/\u001a\u00020\u00142\u0006\u00100\u001a\u00020$H\u0002J\u0010\u00101\u001a\u00020\u00142\u0006\u00100\u001a\u00020$H\u0002J\u0010\u00102\u001a\u00020$2\u0006\u00103\u001a\u000204H\u0002J\b\u00105\u001a\u00020\u0014H\u0002J\b\u00106\u001a\u00020\u0014H\u0002J\b\u00107\u001a\u00020\u0014H\u0016J\u0014\u00108\u001a\u0004\u0018\u0001092\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006;"}, d2 = {"Lcom/streamlite/stream/StreamingService;", "Landroid/app/Service;", "Lcom/pedro/common/ConnectChecker;", "<init>", "()V", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "projectionManager", "Landroid/media/projection/MediaProjectionManager;", "projection", "Landroid/media/projection/MediaProjection;", "stream", "Lcom/pedro/library/generic/GenericStream;", "ticker", "Lkotlinx/coroutines/Job;", "startedAt", "", "selectedFps", "", "onCreate", "", "onStartCommand", "intent", "Landroid/content/Intent;", "flags", "startId", "begin", "createAudioSource", "Lcom/pedro/encoder/input/sources/audio/AudioSource;", "type", "Lcom/streamlite/core/AudioSource;", "mediaProjection", "onFps", "fps", "onConnectionStarted", "url", "", "(Ljava/lang/String;)Ljava/lang/Integer;", "onConnectionSuccess", "onNewBitrate", "bitrate", "onConnectionFailed", "reason", "onDisconnect", "onAuthError", "onAuthSuccess", "()Ljava/lang/Integer;", "stopStreaming", "message", "fail", "readableMessage", "t", "", "createChannel", "startForegroundNow", "onDestroy", "onBind", "Landroid/os/IBinder;", "Companion", "app_debug"})
public final class StreamingService extends android.app.Service implements com.pedro.common.ConnectChecker {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "streaming";
    private static final int NOTIFICATION_ID = 1001;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ACTION_START = "com.streamlite.action.START";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ACTION_STOP = "com.streamlite.action.STOP";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_RESULT_CODE = "result_code";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_PROJECTION_DATA = "projection_data";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_WIDTH = "width";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_HEIGHT = "height";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_FPS = "fps";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_BITRATE = "bitrate";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_AUDIO = "audio";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_ENDPOINT = "endpoint";
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.streamlite.core.StreamStats> mutableStats = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.StateFlow<com.streamlite.core.StreamStats> stats = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private android.media.projection.MediaProjectionManager projectionManager;
    @org.jetbrains.annotations.Nullable()
    private android.media.projection.MediaProjection projection;
    @org.jetbrains.annotations.Nullable()
    private com.pedro.library.generic.GenericStream stream;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job ticker;
    private long startedAt = 0L;
    private int selectedFps = 60;
    @org.jetbrains.annotations.NotNull()
    public static final com.streamlite.stream.StreamingService.Companion Companion = null;
    
    public StreamingService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void begin(android.content.Intent intent) {
    }
    
    private final com.pedro.encoder.input.sources.audio.AudioSource createAudioSource(com.streamlite.core.AudioSource type, android.media.projection.MediaProjection mediaProjection) {
        return null;
    }
    
    private final void onFps(int fps) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.Integer onConnectionStarted(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @java.lang.Override()
    public void onConnectionSuccess() {
    }
    
    @java.lang.Override()
    public void onNewBitrate(long bitrate) {
    }
    
    @java.lang.Override()
    public void onConnectionFailed(@org.jetbrains.annotations.NotNull()
    java.lang.String reason) {
    }
    
    @java.lang.Override()
    public void onDisconnect() {
    }
    
    @java.lang.Override()
    public void onAuthError() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.Integer onAuthSuccess() {
        return null;
    }
    
    private final void stopStreaming(java.lang.String message) {
    }
    
    private final void fail(java.lang.String message) {
    }
    
    private final java.lang.String readableMessage(java.lang.Throwable t) {
        return null;
    }
    
    private final void createChannel() {
    }
    
    private final void startForegroundNow() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J&\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u00072\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!J\u000e\u0010\"\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00140\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006#"}, d2 = {"Lcom/streamlite/stream/StreamingService$Companion;", "", "<init>", "()V", "CHANNEL_ID", "", "NOTIFICATION_ID", "", "ACTION_START", "ACTION_STOP", "EXTRA_RESULT_CODE", "EXTRA_PROJECTION_DATA", "EXTRA_WIDTH", "EXTRA_HEIGHT", "EXTRA_FPS", "EXTRA_BITRATE", "EXTRA_AUDIO", "EXTRA_ENDPOINT", "mutableStats", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/streamlite/core/StreamStats;", "stats", "Lkotlinx/coroutines/flow/StateFlow;", "getStats", "()Lkotlinx/coroutines/flow/StateFlow;", "start", "", "context", "Landroid/content/Context;", "resultCode", "projectionData", "Landroid/content/Intent;", "config", "Lcom/streamlite/core/StreamConfig;", "stop", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.StateFlow<com.streamlite.core.StreamStats> getStats() {
            return null;
        }
        
        public final void start(@org.jetbrains.annotations.NotNull()
        android.content.Context context, int resultCode, @org.jetbrains.annotations.NotNull()
        android.content.Intent projectionData, @org.jetbrains.annotations.NotNull()
        com.streamlite.core.StreamConfig config) {
        }
        
        public final void stop(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
    }
}