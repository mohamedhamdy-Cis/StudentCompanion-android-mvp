package com.studentcompanion.app.domain.usecase.parent

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import javax.inject.Inject

class UnlinkChildUseCase @Inject constructor(
    private val parentLinkRepository: ParentLinkRepository,
) {
    suspend operator fun invoke(studentId: String): Result<Unit> = parentLinkRepository.unlinkChild(studentId)
}
