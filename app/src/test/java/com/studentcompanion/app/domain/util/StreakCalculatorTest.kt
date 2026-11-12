package com.studentcompanion.app.domain.util

import com.studentcompanion.app.domain.model.PointsEntry
import org.junit.Assert.assertEquals
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Test

class StreakCalculatorTest {

    private val zone = ZoneId.of("Asia/Riyadh")
    private val today = LocalDate.of(2026, 11, 12)

    private fun entryOn(date: LocalDate, label: String = "Fajr prayer"): PointsEntry = PointsEntry(
        id = "id-$date-$label",
        studentId = "student-1",
        taskId = null,
        label = label,
        points = 10,
        earnedAt = date.atTime(6, 0).atZone(zone).toInstant(),
    )

    @Test
    fun `no entries means no streak`() {
        assertEquals(0, StreakCalculator.currentStreakDays(emptyList(), zone, today))
    }

    @Test
    fun `counts consecutive days ending today`() {
        val entries = listOf(
            entryOn(today),
            entryOn(today.minusDays(1)),
            entryOn(today.minusDays(2)),
        )
        assertEquals(3, StreakCalculator.currentStreakDays(entries, zone, today))
    }

    @Test
    fun `stops at the first gap`() {
        val entries = listOf(
            entryOn(today),
            entryOn(today.minusDays(1)),
            // gap at today - 2
            entryOn(today.minusDays(3)),
        )
        assertEquals(2, StreakCalculator.currentStreakDays(entries, zone, today))
    }

    @Test
    fun `streak is zero if today has no prayer entry, even with a past streak`() {
        val entries = listOf(
            entryOn(today.minusDays(1)),
            entryOn(today.minusDays(2)),
        )
        assertEquals(0, StreakCalculator.currentStreakDays(entries, zone, today))
    }

    @Test
    fun `non-prayer entries do not count toward the streak`() {
        val entries = listOf(entryOn(today, label = "Note added"))
        assertEquals(0, StreakCalculator.currentStreakDays(entries, zone, today))
    }

    @Test
    fun `multiple prayer entries on the same day still count as one day`() {
        val entries = listOf(
            entryOn(today, label = "Fajr prayer"),
            entryOn(today, label = "Dhuhr prayer"),
            entryOn(today, label = "Asr prayer"),
        )
        assertEquals(1, StreakCalculator.currentStreakDays(entries, zone, today))
    }

    @Test
    fun `a prayer just after midnight Riyadh time still counts for today, even though it is still yesterday in UTC`() {
        // Riyadh is UTC+3, so 00:30 "today" in Riyadh is 21:30 "yesterday" in UTC. The old
        // implementation bucketed by Instant.toString() (UTC), which would have placed this
        // entry on the wrong day. Asserting against the Riyadh-zoned calculation proves the fix.
        val justAfterMidnightRiyadh = today.atTime(0, 30).atZone(zone).toInstant()
        assertEquals(today.minusDays(1), justAfterMidnightRiyadh.atZone(ZoneOffset.UTC).toLocalDate())

        val entries = listOf(
            PointsEntry(
                id = "early-fajr",
                studentId = "student-1",
                taskId = null,
                label = "Fajr prayer",
                points = 10,
                earnedAt = justAfterMidnightRiyadh,
            ),
        )
        assertEquals(1, StreakCalculator.currentStreakDays(entries, zone, today))
    }
}
