package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.core.util.safeCall
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

    // Last user successfully fetched/returned by the server, kept so a transient
    // /auth/me failure (offline, timeout, 5xx) degrades to "last known user"
    // instead of "logged out" — only an actual missing token (real logout, or a
    // refresh that TokenAuthenticator gave up on and cleared) should report null.
    @Volatile
    private var cachedUser: User? = null

    override fun currentUser(): Flow<User?> = tokenStore.tokens.map { tokens ->
        if (tokens == null) {
            cachedUser = null
            null
        } else {
            runCatching { api.me().toDomain() }
                .onSuccess { cachedUser = it }
                .getOrElse { cachedUser }
        }
    }

    override suspend fun login(username: String, password: String): Result<User> =
        safeCall("Login failed") {
            val response = api.login(LoginRequest(username, password))
            tokenStore.save(AuthTokens(response.accessToken, response.refreshToken))
            response.user.toDomain().also { cachedUser = it }
        }

    // The backend always creates a STUDENT regardless of [role] — self-registration
    // can't elevate its own privileges (see RegisterDto in the backend repo).
    override suspend fun register(
        username: String,
        password: String,
        fullName: String,
        role: UserRole,
    ): Result<User> = safeCall("Registration failed") {
        val response = api.register(RegisterRequest(username, password, fullName))
        tokenStore.save(AuthTokens(response.accessToken, response.refreshToken))
        response.user.toDomain().also { cachedUser = it }
    }

    override suspend fun logout() {
        val refreshToken = tokenStore.current()?.refreshToken
        if (refreshToken != null) {
            runCatching { api.logout(RefreshRequest(refreshToken)) }
        }
        tokenStore.clear()
        cachedUser = null
    }
}
