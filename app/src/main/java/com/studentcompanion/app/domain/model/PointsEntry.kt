package com.studentcompanion.app.domain.model

import java.time.Instant

data class PointsEntry(
    val id: String,
    val studentId: String,
    val taskId: String?,
    val label: String,
    val points: Int,
    val earnedAt: Instant,
)

data class PointsSummary(
    val totalPoints: Int,
    val currentStreakDays: Int,
    val recentEntries: List<PointsEntry>,
)
