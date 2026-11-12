package com.studentcompanion.app.domain.usecase.parent

import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChildTasksUseCase @Inject constructor(
    private val parentLinkRepository: ParentLinkRepository,
) {
    operator fun invoke(studentId: String): Flow<List<Task>> = parentLinkRepository.getChildTasks(studentId)
}
