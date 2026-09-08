package com.studentcompanion.app.domain.usecase.task

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.repository.TaskRepository
import javax.inject.Inject

/** Seeds today's five prayer tasks the first time a student opens Home that day. */
class EnsureTodayPrayerTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(studentId: String): Result<Unit> =
        taskRepository.ensureTodaysPrayerTasks(studentId)
}
