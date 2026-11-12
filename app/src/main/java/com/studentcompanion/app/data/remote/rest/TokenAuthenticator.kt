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

        return synchronized(this) {
            runBlocking {
                val staleAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                val current = tokenStore.current() ?: return@runBlocking null

                if (current.accessToken != staleAccessToken) {
                    // Another request already refreshed while we waited for the lock.
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer ${current.accessToken}")
                        .build()
                }

                val refreshed = runCatching {
                    plainApi.get().refresh(RefreshRequest(current.refreshToken))
                }.getOrNull()

                if (refreshed == null) {
                    tokenStore.clear()
                    return@runBlocking null
                }

                tokenStore.save(AuthTokens(refreshed.accessToken, refreshed.refreshToken))
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshed.accessToken}")
                    .build()
            }
        }
    }

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
