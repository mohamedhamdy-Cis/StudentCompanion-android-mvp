package com.studentcompanion.app.domain.usecase.task

import android.net.Uri
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.repository.PointsRepository
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Validates every attachment against the 5 MB limit, then submits.
 * Fails fast on the first oversized image rather than uploading partially.
 */
class SubmitHomeworkUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val pointsRepository: PointsRepository,
    private val validateAttachment: ValidateAttachmentUseCase,
) {
    suspend operator fun invoke(taskId: String, comment: String?, imageUris: List<Uri>): Result<Unit> {
        if (imageUris.size > 5) return Result.Error("You can attach up to 5 images per submission")
        for (uri in imageUris) {
            val validation = validateAttachment(uri)
            if (validation is Result.Error) return validation
        }
        val submit = taskRepository.submitHomework(taskId, comment, imageUris)
        if (submit is Result.Error) return submit

        val task = taskRepository.getTask(taskId).first()
        if (task != null) {
            val points = task.pointsValue.takeIf { it > 0 } ?: Constants.POINTS_HOMEWORK_SUBMITTED
            pointsRepository.awardPoints(task.studentId, task.id, "Homework: ${task.title}", points)
        }
        return Result.Success(Unit)
    }
}
