package com.studentcompanion.app.testutil

import android.net.Uri
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Minimal in-memory [TaskRepository] test double — no mocking framework needed. */
class FakeTaskRepository(initialTask: Task? = null) : TaskRepository {
    private val tasksById = mutableMapOf<String, MutableStateFlow<Task?>>().apply {
        initialTask?.let { put(it.id, MutableStateFlow(it)) }
    }
    var markCompletedCalls = 0
        private set

    override fun getDailyTasks(studentId: String): Flow<List<Task>> =
        throw NotImplementedError("not needed by these tests")

    override fun getTask(taskId: String): Flow<Task?> =
        tasksById.getOrPut(taskId) { MutableStateFlow(null) }

    override suspend fun ensureTodaysPrayerTasks(studentId: String): Result<Unit> =
        Result.Success(Unit)

    override suspend fun markTaskCompleted(taskId: String, comment: String?): Result<Unit> {
        markCompletedCalls++
        val flow = tasksById.getValue(taskId)
        flow.value = flow.value?.copy(status = com.studentcompanion.app.domain.model.TaskStatus.APPROVED)
        return Result.Success(Unit)
    }

    override suspend fun submitHomework(taskId: String, comment: String?, imageUris: List<Uri>): Result<Unit> =
        throw NotImplementedError("not needed by these tests")
}
