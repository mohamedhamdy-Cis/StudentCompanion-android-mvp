package com.studentcompanion.app.domain.usecase.task

import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Returns a student's prayer + homework tasks, already ordered by dueAt by the repository. */
class GetDailyTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(studentId: String): Flow<List<Task>> = taskRepository.getDailyTasks(studentId)
}
