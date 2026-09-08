package com.studentcompanion.app.domain.usecase.auth

import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = authRepository.currentUser()
}
