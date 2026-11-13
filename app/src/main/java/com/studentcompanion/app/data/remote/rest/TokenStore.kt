package com.studentcompanion.app.data.remote.rest

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class AuthTokens(val accessToken: String, val refreshToken: String)

private val Context.authDataStore by preferencesDataStore(name = "auth_tokens")

/** Persists the access/refresh token pair for the backend. */
@Singleton
class TokenStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val tokens: Flow<AuthTokens?> = context.authDataStore.data.map { prefs ->
        val access = prefs[Keys.ACCESS_TOKEN]
        val refresh = prefs[Keys.REFRESH_TOKEN]
        if (access != null && refresh != null) AuthTokens(access, refresh) else null
    }

    suspend fun current(): AuthTokens? = tokens.first()

    suspend fun save(tokens: AuthTokens) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = tokens.accessToken
            prefs[Keys.REFRESH_TOKEN] = tokens.refreshToken
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}
