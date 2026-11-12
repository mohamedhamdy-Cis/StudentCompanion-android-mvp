package com.studentcompanion.app.data.remote.aladhan

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class PrayerTime(val title: String, val time: LocalTime)

@Singleton
class AladhanPrayerTimesDataSource @Inject constructor(
    private val api: AladhanApi,
) {
    /**
     * Live prayer times for today from the Aladhan API. Throws on any
     * network/parse failure — callers decide the fallback (see
     * [com.studentcompanion.app.data.repository.TaskRepositoryImpl], which
     * falls back to fixed seed times so a flaky connection never blocks the
     * Home screen).
     */
    suspend fun todaysPrayerTimes(
        city: String = DEFAULT_CITY,
        country: String = DEFAULT_COUNTRY,
        method: Int = UMM_AL_QURA_METHOD,
    ): List<PrayerTime> {
        val timings = api.timingsByCity(city, country, method).data.timings
        return listOf(
            PrayerTime("Fajr", timings.Fajr.toLocalTime()),
            PrayerTime("Dhuhr", timings.Dhuhr.toLocalTime()),
            PrayerTime("Asr", timings.Asr.toLocalTime()),
            PrayerTime("Maghrib", timings.Maghrib.toLocalTime()),
            PrayerTime("Isha", timings.Isha.toLocalTime()),
        )
    }

    private fun String.toLocalTime(): LocalTime = LocalTime.parse(take(5), HOUR_MINUTE)

    companion object {
        const val DEFAULT_CITY = "Riyadh"
        const val DEFAULT_COUNTRY = "Saudi Arabia"

        // Umm Al-Qura University, Makkah — the standard calculation method within Saudi Arabia.
        const val UMM_AL_QURA_METHOD = 4

        private val HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm")
    }
}
