package com.studentcompanion.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.data.model.UserDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase has no native username/password auth (only email/password), so
 * usernames are mapped to a synthetic email under the app's own domain.
 * This mapping is fully internal to this data source — nothing above it
 * needs to know a "fake email" is involved.
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private fun usernameToEmail(username: String) = "$username@studentcompanion.local"

    suspend fun login(username: String, password: String): UserDto {
        val result = auth.signInWithEmailAndPassword(usernameToEmail(username), password).await()
        val uid = result.user?.uid ?: error("Login succeeded but no user id was returned")
        val snapshot = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
        return snapshot.toObject(UserDto::class.java)?.copy(id = uid) ?: error("User profile not found")
    }

    suspend fun register(username: String, password: String, fullName: String, role: String): UserDto {
        val result = auth.createUserWithEmailAndPassword(usernameToEmail(username), password).await()
        val uid = result.user?.uid ?: error("Registration succeeded but no user id was returned")
        val userDto = UserDto(id = uid, username = username, fullName = fullName, role = role)
        firestore.collection(Constants.COLLECTION_USERS).document(uid).set(userDto).await()
        return userDto
    }

    fun logout() = auth.signOut()

    fun currentUserId(): String? = auth.currentUser?.uid
}
