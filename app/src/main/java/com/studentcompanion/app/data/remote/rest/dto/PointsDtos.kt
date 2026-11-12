package com.studentcompanion.app.data.remote.rest.dto

data class RestPointsLedgerEntryDto(
    val id: String,
    val studentId: String,
    val taskId: String?,
    val label: String,
    val points: Int,
    val earnedAt: String,
)

data class PointsSummaryResponse(
    val totalPoints: Int,
    val currentStreakDays: Int,
    val recentEntries: List<RestPointsLedgerEntryDto>,
)

data class AwardPointsRequest(
    val taskId: String?,
    val label: String,
    val points: Int,
)
