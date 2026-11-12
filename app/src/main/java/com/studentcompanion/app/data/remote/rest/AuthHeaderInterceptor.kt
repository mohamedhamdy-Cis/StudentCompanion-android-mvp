package com.studentcompanion.app.data.remote.rest

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Attaches the current access token to every request. Refresh-on-401 is [TokenAuthenticator]'s job. */
class AuthHeaderInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // OkHttp interceptors run on a background dispatcher thread, never the
        // main thread, so blocking here for a DataStore read is safe.
        val accessToken = runBlocking { tokenStore.current() }?.accessToken
        val request = chain.request().let { original ->
            if (accessToken == null) {
                original
            } else {
                original.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            }
        }
        return chain.proceed(request)
    }
}
