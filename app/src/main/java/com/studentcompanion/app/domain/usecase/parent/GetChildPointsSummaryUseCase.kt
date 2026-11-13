package com.studentcompanion.app.domain.usecase.parent

import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChildPointsSummaryUseCase @Inject constructor(
    private val parentLinkRepository: ParentLinkRepository,
) {
    operator fun invoke(studentId: String): Flow<PointsSummary> = parentLinkRepository.getChildPointsSummary(studentId)
}
