package com.studentcompanion.app.data.model

import com.google.firebase.Timestamp

data class PointsEntryDto(
    val id: String = "",
    val studentId: String = "",
    val taskId: String? = null,
    val label: String = "",
    val points: Int = 0,
    val earnedAt: Timestamp = Timestamp.now(),
)
