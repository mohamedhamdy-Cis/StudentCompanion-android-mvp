package com.studentcompanion.app.domain.usecase.auth

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank()) {
            return Result.Error("Username and password are required")
        }
        return authRepository.login(username.trim(), password)
    }
}
