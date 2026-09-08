package com.studentcompanion.app.domain.repository

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.PointsSummary
import kotlinx.coroutines.flow.Flow

interface PointsRepository {
    fun getPointsSummary(studentId: String): Flow<PointsSummary>
    suspend fun awardPoints(studentId: String, taskId: String?, label: String, points: Int): Result<Unit>
}
