package com.studentcompanion.app.domain.usecase.points

import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPointsSummaryUseCase @Inject constructor(
    private val pointsRepository: PointsRepository,
) {
    operator fun invoke(studentId: String): Flow<PointsSummary> = pointsRepository.getPointsSummary(studentId)
}
