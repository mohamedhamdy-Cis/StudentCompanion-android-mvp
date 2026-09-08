package com.studentcompanion.app.domain.usecase.task

import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.domain.repository.PointsRepository
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/** Used for prayer tasks and any non-attachment task (e.g. "mark as completed" toggle). */
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val pointsRepository: PointsRepository,
) {
    suspend operator fun invoke(taskId: String, comment: String? = null): Result<Unit> {
        val task = taskRepository.getTask(taskId).first()
            ?: return Result.Error("Task not found")
        if (task.status != TaskStatus.PENDING) {
            return Result.Error("This task is already completed")
        }
        val update = taskRepository.markTaskCompleted(taskId, comment?.trim()?.ifBlank { null })
        if (update is Result.Error) return update

        val onTime = Instant.now().isBefore(task.dueAt.plus(Duration.ofHours(1)))
        val points = if (task.type == TaskType.PRAYER) {
            if (onTime) Constants.POINTS_PRAYER_ON_TIME else Constants.POINTS_PRAYER_LATE
        } else {
            task.pointsValue.takeIf { it > 0 } ?: Constants.POINTS_HOMEWORK_SUBMITTED
        }
        val label = if (task.type == TaskType.PRAYER) {
            if (onTime) "${task.title} prayer" else "${task.title} prayer (late)"
        } else {
            task.title
        }
        pointsRepository.awardPoints(task.studentId, task.id, label, points)
        return Result.Success(Unit)
    }
}
