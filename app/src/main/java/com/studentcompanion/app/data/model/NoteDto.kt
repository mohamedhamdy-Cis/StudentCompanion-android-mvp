package com.studentcompanion.app.data.model

import com.google.firebase.Timestamp

data class NoteDto(
    val id: String = "",
    val studentId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val body: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
)
