package com.studentcompanion.app.domain.repository

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.LinkCode
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Parent <-> student linking never existed on the Firestore-era backend —
 * this is a new, REST-backend-only capability from Sprint 5, so unlike the
 * other domain repositories there is no Firebase implementation to fall
 * back to (see RepositoryModule.kt).
 */
interface ParentLinkRepository {
    suspend fun generateLinkCode(): Result<LinkCode>
    suspend fun redeemCode(code: String): Result<User>
    fun getChildren(): Flow<List<User>>
    suspend fun unlinkChild(studentId: String): Result<Unit>
    fun getChildTasks(studentId: String): Flow<List<Task>>
    fun getChildPointsSummary(studentId: String): Flow<PointsSummary>
}
