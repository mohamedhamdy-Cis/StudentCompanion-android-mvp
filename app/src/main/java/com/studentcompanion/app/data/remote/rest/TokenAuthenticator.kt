package com.studentcompanion.app.data.remote.rest

import com.studentcompanion.app.data.remote.rest.dto.RefreshRequest
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Refreshes the access/refresh token pair on a 401 and retries once.
 * Depends on a [PlainApi]-qualified client (no [AuthHeaderInterceptor] and no
 * authenticator of its own) to call /auth/refresh without recursing back
 * into this authenticator.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    @PlainApi private val plainApi: Lazy<StudentCompanionApi>,
) : Authenticator {

    // OkHttp may call authenticate() concurrently for several in-flight 401s —
    // only the first should hit the network; the rest just retry with its result.
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null // already retried once — give up

        val staleAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        // Fast path: if another caller already refreshed before this one got here,
        // retry with the current token immediately — no lock, no network call, no
        // blocking a dispatcher thread for a refresh this request doesn't need.
        currentTokenIfChanged(staleAccessToken)?.let { return it.toAuthorizedRequest(response) }

        return synchronized(this) {
            runBlocking {
                // Re-check inside the lock: another thread may have refreshed while
                // this one was waiting to acquire it.
                val current = tokenStore.current() ?: return@runBlocking null
                if (current.accessToken != staleAccessToken) {
                    return@runBlocking current.toAuthorizedRequest(response)
                }

                val refreshed = runCatching {
                    plainApi.get().refresh(RefreshRequest(current.refreshToken))
                }.getOrNull()

                if (refreshed == null) {
                    tokenStore.clear()
                    return@runBlocking null
                }

                val newTokens = AuthTokens(refreshed.accessToken, refreshed.refreshToken)
                tokenStore.save(newTokens)
                newTokens.toAuthorizedRequest(response)
            }
        }
    }

    private fun currentTokenIfChanged(staleAccessToken: String?): AuthTokens? =
        runBlocking { tokenStore.current() }?.takeIf { it.accessToken != staleAccessToken }

    private fun AuthTokens.toAuthorizedRequest(response: Response): Request =
        response.request.newBuilder().header("Authorization", "Bearer $accessToken").build()

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
