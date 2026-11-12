package com.studentcompanion.app.data.remote.rest.dto

data class RestNoteDto(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val subjectName: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val updatedAt: String,
)

data class CreateNoteRequest(
    val subjectId: String,
    val title: String,
    val body: String,
)

data class UpdateNoteRequest(
    val title: String,
    val body: String,
)
