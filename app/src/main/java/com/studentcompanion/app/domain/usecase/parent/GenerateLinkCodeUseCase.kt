package com.studentcompanion.app.domain.usecase.parent

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.LinkCode
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import javax.inject.Inject

class GenerateLinkCodeUseCase @Inject constructor(
    private val parentLinkRepository: ParentLinkRepository,
) {
    suspend operator fun invoke(): Result<LinkCode> = parentLinkRepository.generateLinkCode()
}
