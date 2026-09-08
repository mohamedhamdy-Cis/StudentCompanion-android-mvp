package com.studentcompanion.app.domain.usecase.task

import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(taskId: String): Flow<Task?> = taskRepository.getTask(taskId)
}
