package com.studentcompanion.app.domain.usecase.parent

import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChildrenUseCase @Inject constructor(
    private val parentLinkRepository: ParentLinkRepository,
) {
    operator fun invoke(): Flow<List<User>> = parentLinkRepository.getChildren()
}
