package com.studentcompanion.app.data.remote.aladhan

import retrofit2.http.GET
import retrofit2.http.Query

/** https://aladhan.com/prayer-times-api — "Timings By City" endpoint. */
interface AladhanApi {

    @GET("v1/timingsByCity")
    suspend fun timingsByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int,
    ): AladhanTimingsResponse
}

data class AladhanTimingsResponse(val data: AladhanData)
data class AladhanData(val timings: AladhanTimings)

/**
 * Only the fields this app seeds tasks for. Aladhan returns each value as
 * "HH:mm" (sometimes with a trailing " (TZ)" suffix depending on query
 * params) — [AladhanPrayerTimesDataSource] takes just the first 5 characters.
 */
data class AladhanTimings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String,
)
