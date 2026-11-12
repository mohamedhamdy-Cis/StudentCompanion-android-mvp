package com.studentcompanion.app.domain.usecase.parent

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import javax.inject.Inject

class RedeemLinkCodeUseCase @Inject constructor(
    private val parentLinkRepository: ParentLinkRepository,
) {
    suspend operator fun invoke(code: String): Result<User> {
        if (code.isBlank()) return Result.Error("Enter the code your child shared with you")
        return parentLinkRepository.redeemCode(code.trim())
    }
}
