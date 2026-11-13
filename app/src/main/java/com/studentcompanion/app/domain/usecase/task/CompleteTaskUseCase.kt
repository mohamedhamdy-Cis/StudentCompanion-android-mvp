package com.studentcompanion.app.domain.usecase.task

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Used for prayer tasks and any non-attachment task (e.g. "mark as completed" toggle).
 * The backend awards the points when it marks the task complete, so this must not
 * award them again.
 */
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(taskId: String, comment: String? = null): Result<Unit> {
        val task = taskRepository.getTask(taskId).first()
            ?: return Result.Error("Task not found")
        if (task.status != TaskStatus.PENDING) {
            return Result.Error("This task is already completed")
        }
        return taskRepository.markTaskCompleted(taskId, comment?.trim()?.ifBlank { null })
    }
}
