package com.streamlite.ui;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.ExposedDropdownMenuDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.input.PasswordVisualTransformation;
import com.streamlite.core.AudioSource;
import com.streamlite.core.StreamPhase;
import com.streamlite.core.StreamStats;
import com.streamlite.stream.StreamingService;
import java.util.Locale;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u00002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u0012\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a:\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\b0\u000b2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00030\rH\u0003\u001a\u0010\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0010H\u0003\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Colors", "Landroidx/compose/material3/ColorScheme;", "StreamLiteApp", "", "viewModel", "Lcom/streamlite/ui/StreamViewModel;", "Selector", "label", "", "selected", "options", "", "select", "Lkotlin/Function1;", "StatusCard", "stats", "Lcom/streamlite/core/StreamStats;", "app_debug"})
public final class StreamLiteAppKt {
    @org.jetbrains.annotations.NotNull()
    private static final androidx.compose.material3.ColorScheme Colors = null;
    
    @androidx.compose.runtime.Composable()
    public static final void StreamLiteApp(@org.jetbrains.annotations.NotNull()
    com.streamlite.ui.StreamViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void Selector(java.lang.String label, java.lang.String selected, java.util.List<java.lang.String> options, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> select) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatusCard(com.streamlite.core.StreamStats stats) {
    }
}