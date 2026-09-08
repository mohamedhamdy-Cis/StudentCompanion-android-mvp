package com.studentcompanion.app.data.model

data class UserDto(
    val id: String = "",
    val username: String = "",
    val fullName: String = "",
    val role: String = "STUDENT",
    val schoolId: String? = null,
    val classId: String? = null,
    val parentId: String? = null,
)
