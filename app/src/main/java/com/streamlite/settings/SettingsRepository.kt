package com.streamlite.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import com.streamlite.core.AudioSource
import com.streamlite.core.StreamConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.streamSettings by preferencesDataStore("stream_settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
  private object Keys {
    val width = intPreferencesKey("width")
    val height = intPreferencesKey("height")
    val fps = intPreferencesKey("fps")
    val bitrate = intPreferencesKey("bitrate")
    val audio = stringPreferencesKey("audio")
    val url = stringPreferencesKey("url")
    val key = stringPreferencesKey("key")
  }

  val config: Flow<StreamConfig> = context.streamSettings.data.map { p -> p.toConfig() }

  suspend fun save(config: StreamConfig) {
    context.streamSettings.edit { p ->
      p[Keys.width] = config.width
      p[Keys.height] = config.height
      p[Keys.fps] = config.fps
      p[Keys.bitrate] = config.bitrateKbps
      p[Keys.audio] = config.audioSource.name
      p[Keys.url] = config.rtmpsUrl
      p[Keys.key] = config.streamKey
    }
  }

  private fun Preferences.toConfig() = StreamConfig(
    width = this[Keys.width] ?: 1920,
    height = this[Keys.height] ?: 1080,
    fps = this[Keys.fps] ?: 60,
    bitrateKbps = this[Keys.bitrate] ?: 12_000,
    audioSource = runCatching { AudioSource.valueOf(this[Keys.audio] ?: AudioSource.MIXED.name) }.getOrDefault(AudioSource.MIXED),
    rtmpsUrl = this[Keys.url] ?: "",
    streamKey = this[Keys.key] ?: ""
  )
}
