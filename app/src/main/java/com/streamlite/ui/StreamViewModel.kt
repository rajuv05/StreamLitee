package com.streamlite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamlite.core.StreamConfig
import com.streamlite.core.StreamStats
import com.streamlite.settings.SettingsRepository
import com.streamlite.stream.StreamingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(private val settings: SettingsRepository) : ViewModel() {
  data class StartRequest(val config: StreamConfig)
  companion object {
    private val mutableStarts = MutableSharedFlow<StartRequest>(extraBufferCapacity = 1)
    private val mutableErrors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val starts = mutableStarts.asSharedFlow()
    val errors = mutableErrors.asSharedFlow()
    fun reportPermissionDenied() { mutableErrors.tryEmit("Microphone permission is required to stream audio.") }
    fun reportProjectionDenied() { mutableErrors.tryEmit("Screen-capture permission was denied.") }
  }
  val config: StateFlow<StreamConfig> = settings.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamConfig())
  val stats: StateFlow<StreamStats> = StreamingService.stats
  fun save(config: StreamConfig) = viewModelScope.launch { settings.save(config) }
  fun requestStart(config: StreamConfig) {
    val validation = config.validate()
    if (validation != null) mutableErrors.tryEmit(validation) else {
      save(config)
      mutableStarts.tryEmit(StartRequest(config))
    }
  }
}
