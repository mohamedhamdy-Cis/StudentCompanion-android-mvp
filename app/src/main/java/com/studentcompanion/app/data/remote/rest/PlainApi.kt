package com.studentcompanion.app.data.remote.rest

import javax.inject.Qualifier

/** Marks the unauthenticated [StudentCompanionApi]/[okhttp3.OkHttpClient] — see [TokenAuthenticator]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlainApi

/** Marks the [okhttp3.OkHttpClient] carrying [AuthHeaderInterceptor] + [TokenAuthenticator]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedClient
