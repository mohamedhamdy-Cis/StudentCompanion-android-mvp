package com.studentcompanion.app.data.remote.rest.dto

// DTOs for the NestJS backend (see docs/API_CONTRACT.md in the backend repo).
// Field names match the JSON wire format exactly — Gson maps by name.

data class RestUserDto(
    val id: String,
    val username: String,
    val fullName: String,
    val role: String,
    val schoolId: String?,
    val classId: String?,
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val role: String,
)

data class LoginRequest(
    val username: String,
    val password: String,
)

data class RefreshRequest(
    val refreshToken: String,
)

data class AuthResponse(
    val user: RestUserDto,
    val accessToken: String,
    val refreshToken: String,
)

data class TokenPairResponse(
    val accessToken: String,
    val refreshToken: String,
)
