package com.studentcompanion.app.domain.model

import java.time.Instant

enum class TaskType { PRAYER, HOMEWORK }

enum class TaskStatus { PENDING, SUBMITTED, APPROVED, NEEDS_REVISION }

data class TaskAttachment(
    val id: String,
    val url: String,
    val sizeBytes: Long,
)

data class Task(
    val id: String,
    val studentId: String,
    val type: TaskType,
    val subjectId: String? = null,     // null for prayer tasks
    val subjectName: String? = null,
    val title: String,
    val description: String = "",
    val dueAt: Instant,
    val status: TaskStatus,
    val pointsValue: Int,
    val studentComment: String? = null,
    val teacherComment: String? = null,
    val attachments: List<TaskAttachment> = emptyList(),
)
