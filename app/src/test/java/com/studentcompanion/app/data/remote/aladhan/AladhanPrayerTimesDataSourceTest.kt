package com.studentcompanion.app.data.remote.aladhan

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class AladhanPrayerTimesDataSourceTest {

    private fun dataSource(timings: AladhanTimings) = AladhanPrayerTimesDataSource(
        api = object : AladhanApi {
            override suspend fun timingsByCity(city: String, country: String, method: Int) =
                AladhanTimingsResponse(AladhanData(timings))
        },
    )

    @Test
    fun `parses plain HH-mm timings into the matching prayer list`() = runTest {
        val source = dataSource(
            AladhanTimings(Fajr = "05:12", Dhuhr = "11:47", Asr = "15:09", Maghrib = "17:52", Isha = "19:22"),
        )

        val prayers = source.todaysPrayerTimes()

        assertEquals(
            listOf(
                PrayerTime("Fajr", LocalTime.of(5, 12)),
                PrayerTime("Dhuhr", LocalTime.of(11, 47)),
                PrayerTime("Asr", LocalTime.of(15, 9)),
                PrayerTime("Maghrib", LocalTime.of(17, 52)),
                PrayerTime("Isha", LocalTime.of(19, 22)),
            ),
            prayers,
        )
    }

    @Test
    fun `ignores a trailing timezone suffix Aladhan sometimes appends`() = runTest {
        val source = dataSource(
            AladhanTimings(
                Fajr = "05:12 (+03)",
                Dhuhr = "11:47 (+03)",
                Asr = "15:09 (+03)",
                Maghrib = "17:52 (+03)",
                Isha = "19:22 (+03)",
            ),
        )

        val prayers = source.todaysPrayerTimes()

        assertEquals(LocalTime.of(5, 12), prayers.first { it.title == "Fajr" }.time)
        assertEquals(LocalTime.of(19, 22), prayers.first { it.title == "Isha" }.time)
    }
}
