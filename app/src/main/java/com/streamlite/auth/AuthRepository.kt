package com.streamlite.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore("auth_prefs")

@Singleton
class AuthRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val tokenKey = stringPreferencesKey("auth_token")

    val token: Flow<String?> = context.authDataStore.data.map { it[tokenKey] }

    suspend fun saveToken(token: String) {
        context.authDataStore.edit { it[tokenKey] = token }
    }

    suspend fun clearToken() {
        context.authDataStore.edit { it.remove(tokenKey) }
    }
}
