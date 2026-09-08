package com.studentcompanion.app.data.model

import com.google.firebase.Timestamp

data class TaskAttachmentDto(
    val id: String = "",
    val url: String = "",
    val sizeBytes: Long = 0,
)

data class TaskDto(
    val id: String = "",
    val studentId: String = "",
    val type: String = "HOMEWORK",
    val subjectId: String? = null,
    val subjectName: String? = null,
    val title: String = "",
    val description: String = "",
    val dueAt: Timestamp = Timestamp.now(),
    val status: String = "PENDING",
    val pointsValue: Int = 0,
    val studentComment: String? = null,
    val teacherComment: String? = null,
    val attachments: List<TaskAttachmentDto> = emptyList(),
)
