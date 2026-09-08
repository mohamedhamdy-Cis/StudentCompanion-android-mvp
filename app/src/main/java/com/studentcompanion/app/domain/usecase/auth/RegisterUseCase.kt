package com.studentcompanion.app.domain.usecase.auth

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.model.UserRole
import com.studentcompanion.app.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        fullName: String,
        role: UserRole,
    ): Result<User> {
        if (username.length < 4) return Result.Error("Username must be at least 4 characters")
        if (password.length < 6) return Result.Error("Password must be at least 6 characters")
        if (fullName.isBlank()) return Result.Error("Full name is required")
        return authRepository.register(username.trim(), password, fullName.trim(), role)
    }
}
