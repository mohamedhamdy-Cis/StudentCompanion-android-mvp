package com.studentcompanion.app.domain.util

import com.studentcompanion.app.domain.model.PointsEntry
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pure, timezone-aware streak logic, kept out of PointsRepositoryImpl so it's
 * testable without a Firestore dependency. "Streak" = consecutive calendar
 * days, ending today, with at least one prayer-related points entry.
 */
object StreakCalculator {

    fun currentStreakDays(
        entries: List<PointsEntry>,
        zone: ZoneId = ZoneId.of("Asia/Riyadh"),
        today: LocalDate = LocalDate.now(zone),
    ): Int {
        val prayerDays = entries
            .filter { it.label.contains("Prayer", ignoreCase = true) }
            .map { it.earnedAt.atZone(zone).toLocalDate() }
            .toSet()

        var streak = 0
        var day = today
        while (prayerDays.contains(day)) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }
}
