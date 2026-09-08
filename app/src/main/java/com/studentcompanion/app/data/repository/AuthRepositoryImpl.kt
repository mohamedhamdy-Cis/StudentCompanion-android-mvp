package com.studentcompanion.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.mapper.toDomain
import com.studentcompanion.app.data.model.UserDto
import com.studentcompanion.app.data.remote.firebase.FirebaseAuthDataSource
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.model.UserRole
import com.studentcompanion.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    // Reacts to Firebase's own auth-state changes (login/logout/session expiry)
    // rather than a one-shot read, so the UI stays in sync automatically.
    override fun currentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                trySend(null)
            } else {
                firestore.collection(Constants.COLLECTION_USERS).document(uid).get()
                    .addOnSuccessListener { snapshot ->
                        trySend(snapshot.toObject(UserDto::class.java)?.copy(id = uid)?.toDomain())
                    }
                    .addOnFailureListener { trySend(null) }
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(username: String, password: String): Result<User> = try {
        Result.Success(authDataSource.login(username, password).toDomain())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Login failed", e)
    }

    override suspend fun register(
        username: String,
        password: String,
        fullName: String,
        role: UserRole,
    ): Result<User> = try {
        Result.Success(authDataSource.register(username, password, fullName, role.name).toDomain())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Registration failed", e)
    }

    override suspend fun logout() = authDataSource.logout()
}
