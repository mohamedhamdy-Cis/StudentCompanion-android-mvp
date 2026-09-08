package com.studentcompanion.app.domain.repository

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Auth contract lives in the domain layer so ViewModels/use cases never
 * know whether login is backed by Firebase Auth or a future REST endpoint.
 */
interface AuthRepository {
    fun currentUser(): Flow<User?>
    suspend fun login(username: String, password: String): Result<User>
    suspend fun register(
        username: String,
        password: String,
        fullName: String,
        role: UserRole,
    ): Result<User>
    suspend fun logout()
}
