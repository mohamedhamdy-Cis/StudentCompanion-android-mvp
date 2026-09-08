package com.studentcompanion.app.domain.model

import java.time.Instant

data class Note(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val subjectName: String,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
