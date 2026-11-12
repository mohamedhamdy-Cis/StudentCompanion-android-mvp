package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.remote.rest.AuthTokens
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.TokenStore
import com.studentcompanion.app.data.remote.rest.dto.LoginRequest
import com.studentcompanion.app.data.remote.rest.dto.RefreshRequest
import com.studentcompanion.app.data.remote.rest.dto.RegisterRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.model.UserRole
import com.studentcompanion.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RestAuthRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
    private val tokenStore: TokenStore,
) : AuthRepository {

    // Re-derives the current user from /auth/me whenever the stored tokens
    // change (login, logout, or a background refresh) — no separate cache to
    // keep in sync, at the cost of one extra call per token rotation.
    override fun currentUser(): Flow<User?> = tokenStore.tokens.map { tokens ->
        if (tokens == null) null else runCatching { api.me().toDomain() }.getOrNull()
    }

    override suspend fun login(username: String, password: String): Result<User> = try {
        val response = api.login(LoginRequest(username, password))
        tokenStore.save(AuthTokens(response.accessToken, response.refreshToken))
        Result.Success(response.user.toDomain())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Login failed", e)
    }

    // The backend always creates a STUDENT regardless of [role] — self-registration
    // can't elevate its own privileges (see RegisterDto in the backend repo).
    override suspend fun register(
        username: String,
        password: String,
        fullName: String,
        role: UserRole,
    ): Result<User> = try {
        val response = api.register(RegisterRequest(username, password, fullName))
        tokenStore.save(AuthTokens(response.accessToken, response.refreshToken))
        Result.Success(response.user.toDomain())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Registration failed", e)
    }

    override suspend fun logout() {
        val refreshToken = tokenStore.current()?.refreshToken
        if (refreshToken != null) {
            runCatching { api.logout(RefreshRequest(refreshToken)) }
        }
        tokenStore.clear()
    }
}
