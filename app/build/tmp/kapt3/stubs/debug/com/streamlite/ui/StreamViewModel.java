package com.streamlite.ui;

import androidx.lifecycle.ViewModel;
import com.streamlite.core.StreamConfig;
import com.streamlite.core.StreamStats;
import com.streamlite.settings.SettingsRepository;
import com.streamlite.stream.StreamingService;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00132\u00020\u0001:\u0002\u0012\u0013B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0006\u001a\u00020\bJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0006\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\n\u00a8\u0006\u0014"}, d2 = {"Lcom/streamlite/ui/StreamViewModel;", "Landroidx/lifecycle/ViewModel;", "settings", "Lcom/streamlite/settings/SettingsRepository;", "<init>", "(Lcom/streamlite/settings/SettingsRepository;)V", "config", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/streamlite/core/StreamConfig;", "getConfig", "()Lkotlinx/coroutines/flow/StateFlow;", "stats", "Lcom/streamlite/core/StreamStats;", "getStats", "save", "Lkotlinx/coroutines/Job;", "requestStart", "", "StartRequest", "Companion", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class StreamViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.streamlite.settings.SettingsRepository settings = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableSharedFlow<com.streamlite.ui.StreamViewModel.StartRequest> mutableStarts = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableSharedFlow<java.lang.String> mutableErrors = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.SharedFlow<com.streamlite.ui.StreamViewModel.StartRequest> starts = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.SharedFlow<java.lang.String> errors = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.streamlite.core.StreamConfig> config = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.streamlite.core.StreamStats> stats = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.streamlite.ui.StreamViewModel.Companion Companion = null;
    
    @javax.inject.Inject()
    public StreamViewModel(@org.jetbrains.annotations.NotNull()
    com.streamlite.settings.SettingsRepository settings) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.streamlite.core.StreamConfig> getConfig() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.streamlite.core.StreamStats> getStats() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job save(@org.jetbrains.annotations.NotNull()
    com.streamlite.core.StreamConfig config) {
        return null;
    }
    
    public final void requestStart(@org.jetbrains.annotations.NotNull()
    com.streamlite.core.StreamConfig config) {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u000f\u001a\u00020\u0010J\u0006\u0010\u0011\u001a\u00020\u0010R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00060\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\f\u00a8\u0006\u0012"}, d2 = {"Lcom/streamlite/ui/StreamViewModel$Companion;", "", "<init>", "()V", "mutableStarts", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/streamlite/ui/StreamViewModel$StartRequest;", "mutableErrors", "", "starts", "Lkotlinx/coroutines/flow/SharedFlow;", "getStarts", "()Lkotlinx/coroutines/flow/SharedFlow;", "errors", "getErrors", "reportPermissionDenied", "", "reportProjectionDenied", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.SharedFlow<com.streamlite.ui.StreamViewModel.StartRequest> getStarts() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.SharedFlow<java.lang.String> getErrors() {
            return null;
        }
        
        public final void reportPermissionDenied() {
        }
        
        public final void reportProjectionDenied() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0011"}, d2 = {"Lcom/streamlite/ui/StreamViewModel$StartRequest;", "", "config", "Lcom/streamlite/core/StreamConfig;", "<init>", "(Lcom/streamlite/core/StreamConfig;)V", "getConfig", "()Lcom/streamlite/core/StreamConfig;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class StartRequest {
        @org.jetbrains.annotations.NotNull()
        private final com.streamlite.core.StreamConfig config = null;
        
        public StartRequest(@org.jetbrains.annotations.NotNull()
        com.streamlite.core.StreamConfig config) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.streamlite.core.StreamConfig getConfig() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.streamlite.core.StreamConfig component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.streamlite.ui.StreamViewModel.StartRequest copy(@org.jetbrains.annotations.NotNull()
        com.streamlite.core.StreamConfig config) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}