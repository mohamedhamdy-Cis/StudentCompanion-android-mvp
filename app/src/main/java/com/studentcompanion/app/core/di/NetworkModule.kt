package com.studentcompanion.app.core.di

import com.studentcompanion.app.BuildConfig
import com.studentcompanion.app.data.remote.aladhan.AladhanApi
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

/** Retrofit wiring for third-party REST APIs (currently just Aladhan). */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideAladhanApi(client: OkHttpClient): AladhanApi = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AladhanApi::class.java)
}
