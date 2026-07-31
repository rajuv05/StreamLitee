package com.streamlite.ui;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.rounded.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.text.input.PasswordVisualTransformation;
import androidx.compose.ui.text.input.VisualTransformation;
import androidx.compose.ui.text.style.TextOverflow;
import com.streamlite.core.AudioSource;
import com.streamlite.core.StreamConfig;
import com.streamlite.core.StreamPhase;
import com.streamlite.core.StreamStats;
import com.streamlite.stream.StreamingService;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000j\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\u001a\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0007\u001a\u0010\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0003\u001a\u0010\u0010\u0011\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0003\u001a<\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u00142\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0018H\u0003\u001a,\u0010\u001a\u001a\u00020\u000b2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0016\u001a\u00020\u00142\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u001c\u0012\u0004\u0012\u00020\u000b0\u001eH\u0003\u001aJ\u0010\u001f\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020!2\u0006\u0010#\u001a\u00020$2\u0006\u0010\u0016\u001a\u00020\u00142\f\u0010%\u001a\b\u0012\u0004\u0012\u00020!0&2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\u000b0\u001eH\u0003\u001a\u0010\u0010(\u001a\u00020\u000b2\u0006\u0010)\u001a\u00020*H\u0003\u001a,\u0010+\u001a\u00020\u000b2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0016\u001a\u00020\u00142\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u001c\u0012\u0004\u0012\u00020\u000b0\u001eH\u0003\u001aF\u0010,\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020!2\u0006\u0010#\u001a\u00020$2\u0006\u0010\u0016\u001a\u00020\u00142\b\b\u0002\u0010-\u001a\u00020\u00142\u0012\u0010.\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\u000b0\u001eH\u0003\u001aB\u0010/\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020!2\u0006\u00100\u001a\u00020!2\u0006\u0010-\u001a\u00020\u00142\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00182\u0012\u0010.\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\u000b0\u001eH\u0003\u001a\u0018\u00102\u001a\u00020!2\u0006\u00103\u001a\u0002042\u0006\u00105\u001a\u000204H\u0002\u001a\u0010\u00106\u001a\u00020!2\u0006\u00107\u001a\u000208H\u0002\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0007\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\t\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u00a8\u00069"}, d2 = {"DarkBackground", "Landroidx/compose/ui/graphics/Color;", "J", "DarkCard", "DarkCardBorder", "PrimaryRed", "SuccessGreen", "WarningYellow", "TextPrimary", "TextSecondary", "StreamLiteApp", "", "viewModel", "Lcom/streamlite/ui/StreamViewModel;", "HeaderSection", "phase", "Lcom/streamlite/core/StreamPhase;", "StatusChip", "GoLiveButtonSection", "isStreaming", "", "isBusy", "enabled", "onStart", "Lkotlin/Function0;", "onStop", "ConfigSection", "config", "Lcom/streamlite/core/StreamConfig;", "onConfigChange", "Lkotlin/Function1;", "ConfigCard", "title", "", "value", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "options", "", "onSelect", "StatsDashboard", "stats", "Lcom/streamlite/core/StreamStats;", "UrlKeySection", "PremiumUrlKeyCard", "isSecret", "onSave", "EditDestinationDialog", "initialValue", "onDismiss", "resolutionLabel", "width", "", "height", "formatElapsedTime", "seconds", "", "app_debug"})
public final class StreamLiteAppKt {
    private static final long DarkBackground = 0L;
    private static final long DarkCard = 0L;
    private static final long DarkCardBorder = 0L;
    private static final long PrimaryRed = 0L;
    private static final long SuccessGreen = 0L;
    private static final long WarningYellow = 0L;
    private static final long TextPrimary = 0L;
    private static final long TextSecondary = 0L;
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void StreamLiteApp(@org.jetbrains.annotations.NotNull()
    com.streamlite.ui.StreamViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void HeaderSection(com.streamlite.core.StreamPhase phase) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatusChip(com.streamlite.core.StreamPhase phase) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void GoLiveButtonSection(boolean isStreaming, boolean isBusy, boolean enabled, kotlin.jvm.functions.Function0<kotlin.Unit> onStart, kotlin.jvm.functions.Function0<kotlin.Unit> onStop) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConfigSection(com.streamlite.core.StreamConfig config, boolean enabled, kotlin.jvm.functions.Function1<? super com.streamlite.core.StreamConfig, kotlin.Unit> onConfigChange) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConfigCard(java.lang.String title, java.lang.String value, androidx.compose.ui.graphics.vector.ImageVector icon, boolean enabled, java.util.List<java.lang.String> options, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSelect) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatsDashboard(com.streamlite.core.StreamStats stats) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void UrlKeySection(com.streamlite.core.StreamConfig config, boolean enabled, kotlin.jvm.functions.Function1<? super com.streamlite.core.StreamConfig, kotlin.Unit> onConfigChange) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void PremiumUrlKeyCard(java.lang.String title, java.lang.String value, androidx.compose.ui.graphics.vector.ImageVector icon, boolean enabled, boolean isSecret, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSave) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void EditDestinationDialog(java.lang.String title, java.lang.String initialValue, boolean isSecret, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSave) {
    }
    
    private static final java.lang.String resolutionLabel(int width, int height) {
        return null;
    }
    
    private static final java.lang.String formatElapsedTime(long seconds) {
        return null;
    }
}