package com.studentcompanion.app.core.di

import com.studentcompanion.app.BuildConfig
import com.studentcompanion.app.data.remote.rest.AuthHeaderInterceptor
import com.studentcompanion.app.data.remote.rest.AuthedClient
import com.studentcompanion.app.data.remote.rest.PlainApi
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Retrofit wiring for the NestJS backend (Sprint 2-4). Two OkHttpClients:
 * [PlainApi] (logging only) breaks the circular dependency a [TokenAuthenticator]
 * would otherwise have on the client it authenticates for; [AuthedClient] is
 * what every real repository call goes through.
 */
@Module
@InstallIn(SingletonComponent::class)
object RestNetworkModule {

    private fun baseLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    @Provides
    @Singleton
    @PlainApi
    fun providePlainOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(baseLoggingInterceptor())
        .build()

    @Provides
    @Singleton
    @PlainApi
    fun providePlainApi(@PlainApi client: OkHttpClient): StudentCompanionApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StudentCompanionApi::class.java)

    @Provides
    @Singleton
    @AuthedClient
    fun provideAuthedOkHttpClient(
        authHeaderInterceptor: AuthHeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(authHeaderInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(baseLoggingInterceptor())
        .build()

    @Provides
    @Singleton
    fun provideStudentCompanionApi(@AuthedClient client: OkHttpClient): StudentCompanionApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StudentCompanionApi::class.java)
}
