package com.studentcompanion.app.domain.model

enum class UserRole { STUDENT, PARENT, TEACHER }

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val schoolId: String? = null,
    val classId: String? = null,
    val parentId: String? = null, // set for Student users
)
