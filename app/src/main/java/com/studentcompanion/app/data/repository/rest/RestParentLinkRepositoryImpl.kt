package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.core.util.safeCall
import com.studentcompanion.app.data.remote.rest.RestDataRefreshBus
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.RedeemCodeRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.LinkCode
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class RestParentLinkRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
    private val refreshBus: RestDataRefreshBus,
) : ParentLinkRepository {

    override suspend fun generateLinkCode(): Result<LinkCode> =
        safeCall("Couldn't generate a code") { api.generateLinkCode().toDomain() }

    override suspend fun redeemCode(code: String): Result<User> =
        safeCall("Couldn't link to that student") {
            api.redeemLinkCode(RedeemCodeRequest(code)).toDomain()
        }.also { if (it is Result.Success) refreshBus.notifyChanged() }

    override fun getChildren(): Flow<List<User>> =
        refreshBus.changes.onStart { emit(Unit) }
            .map { api.getChildren().map { child -> child.toDomain() } }
            .catch { emit(emptyList()) }

    override suspend fun unlinkChild(studentId: String): Result<Unit> =
        safeCall("Couldn't unlink") { api.unlinkChild(studentId) }
            .also { if (it is Result.Success) refreshBus.notifyChanged() }

    override fun getChildTasks(studentId: String): Flow<List<Task>> =
        refreshBus.changes.onStart { emit(Unit) }
            .map { api.getChildTasks(studentId).map { it.toDomain() } }
            .catch { emit(emptyList()) }

    override fun getChildPointsSummary(studentId: String): Flow<PointsSummary> =
        refreshBus.changes.onStart { emit(Unit) }
            .map {
                val response = api.getChildPointsSummary(studentId)
                PointsSummary(
                    totalPoints = response.totalPoints,
                    currentStreakDays = response.currentStreakDays,
                    recentEntries = response.recentEntries.map { it.toDomain() },
                )
            }
            .catch { emit(PointsSummary(totalPoints = 0, currentStreakDays = 0, recentEntries = emptyList())) }
}
