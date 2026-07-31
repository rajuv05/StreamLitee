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

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u001a$\u0010\u0000\u001a\u0004\u0018\u0001H\u0001\"\u0006\b\u0000\u0010\u0001\u0018\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0082\b\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"getParcelableExtraCompat", "T", "Landroid/content/Intent;", "key", "", "(Landroid/content/Intent;Ljava/lang/String;)Ljava/lang/Object;", "app_debug"})
public final class StreamingServiceKt {
}