package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.AwardPointsRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RestPointsRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
) : PointsRepository {

    override fun getPointsSummary(studentId: String): Flow<PointsSummary> = flow {
        val response = api.getPointsSummary()
        emit(
            PointsSummary(
                totalPoints = response.totalPoints,
                currentStreakDays = response.currentStreakDays,
                recentEntries = response.recentEntries.map { it.toDomain() },
            ),
        )
    }

    override suspend fun awardPoints(
        studentId: String,
        taskId: String?,
        label: String,
        points: Int,
    ): Result<Unit> = try {
        api.awardPoints(AwardPointsRequest(taskId, label, points))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't award points", e)
    }
}
