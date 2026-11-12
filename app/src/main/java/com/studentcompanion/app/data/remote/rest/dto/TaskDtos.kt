package com.studentcompanion.app.data.remote.rest.dto

data class RestTaskAttachmentDto(
    val id: String,
    val url: String,
    val sizeBytes: String, // BigInt serialized as a string by the backend
)

data class RestTaskDto(
    val id: String,
    val studentId: String,
    val type: String,
    val subjectId: String?,
    val title: String,
    val description: String,
    val dueAt: String,
    val status: String,
    val pointsValue: Int,
    val studentComment: String?,
    val teacherComment: String?,
    val attachments: List<RestTaskAttachmentDto>,
)

data class CompleteTaskRequest(val comment: String?)

data class SubmitHomeworkAttachment(val url: String, val sizeBytes: Long)
data class SubmitHomeworkRequest(
    val comment: String?,
    val attachments: List<SubmitHomeworkAttachment>,
)

data class PresignUploadRequest(val contentType: String, val sizeBytes: Long)
data class PresignUploadResponse(
    val uploadUrl: String,
    val publicUrl: String,
    val key: String,
    val expiresInSeconds: Int,
)
