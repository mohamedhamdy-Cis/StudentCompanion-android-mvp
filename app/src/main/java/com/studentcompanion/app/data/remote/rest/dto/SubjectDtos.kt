package com.studentcompanion.app.data.remote.rest.dto

data class RestSubjectDto(
    val id: String,
    val studentId: String,
    val name: String,
    val colorHex: String,
)

data class CreateSubjectRequest(
    val name: String,
)
