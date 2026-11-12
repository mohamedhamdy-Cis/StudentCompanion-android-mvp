package com.studentcompanion.app.data.repository

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.mapper.toDomain
import com.studentcompanion.app.data.remote.firebase.FirestorePointsDataSource
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.repository.PointsRepository
import com.studentcompanion.app.domain.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PointsRepositoryImpl @Inject constructor(
    private val pointsDataSource: FirestorePointsDataSource,
) : PointsRepository {

    override fun getPointsSummary(studentId: String): Flow<PointsSummary> =
        pointsDataSource.observeLedger(studentId).map { entries ->
            val domainEntries = entries.map { it.toDomain() }.sortedByDescending { it.earnedAt }
            PointsSummary(
                totalPoints = domainEntries.sumOf { it.points },
                currentStreakDays = StreakCalculator.currentStreakDays(domainEntries),
                recentEntries = domainEntries.take(20),
            )
        }

    override suspend fun awardPoints(
        studentId: String,
        taskId: String?,
        label: String,
        points: Int,
    ): Result<Unit> = try {
        pointsDataSource.awardPoints(studentId, taskId, label, points)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't award points", e)
    }
}
