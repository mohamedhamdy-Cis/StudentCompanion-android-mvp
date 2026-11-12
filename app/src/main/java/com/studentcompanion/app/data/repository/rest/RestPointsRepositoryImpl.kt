package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.core.util.safeCall
import com.studentcompanion.app.data.remote.rest.RestDataRefreshBus
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.AwardPointsRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class RestPointsRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
    private val refreshBus: RestDataRefreshBus,
) : PointsRepository {

    override fun getPointsSummary(studentId: String): Flow<PointsSummary> =
        refreshBus.changes.onStart { emit(Unit) }
            .map {
                val summary = api.getPointsSummary()
                PointsSummary(
                    totalPoints = summary.totalPoints,
                    currentStreakDays = summary.currentStreakDays,
                    recentEntries = summary.recentEntries.map { it.toDomain() },
                )
            }
            .catch { emit(PointsSummary(totalPoints = 0, currentStreakDays = 0, recentEntries = emptyList())) }

    override suspend fun awardPoints(
        studentId: String,
        taskId: String?,
        label: String,
        points: Int,
    ): Result<Unit> = safeCall("Couldn't award points") {
        api.awardPoints(AwardPointsRequest(taskId, label, points))
    }.also { if (it is Result.Success) refreshBus.notifyChanged() }
}
