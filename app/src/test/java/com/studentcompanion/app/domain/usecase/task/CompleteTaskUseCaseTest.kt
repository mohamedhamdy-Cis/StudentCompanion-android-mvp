package com.studentcompanion.app.domain.usecase.task

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.testutil.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CompleteTaskUseCaseTest {

    private fun prayerTask(status: TaskStatus = TaskStatus.PENDING) = Task(
        id = "task-1",
        studentId = "student-1",
        type = TaskType.PRAYER,
        title = "Fajr",
        dueAt = Instant.now(),
        status = status,
        pointsValue = 10,
    )

    @Test
    fun `completing a pending task marks it completed exactly once`() = runTest {
        val task = prayerTask()
        val taskRepository = FakeTaskRepository(task)
        val useCase = CompleteTaskUseCase(taskRepository)

        val result = useCase(task.id)

        assertTrue(result is Result.Success)
        assertEquals(1, taskRepository.markCompletedCalls)
    }

    @Test
    fun `completing an already-completed task returns an error without calling the repository`() = runTest {
        val task = prayerTask(status = TaskStatus.APPROVED)
        val taskRepository = FakeTaskRepository(task)
        val useCase = CompleteTaskUseCase(taskRepository)

        val result = useCase(task.id)

        assertTrue(result is Result.Error)
        assertEquals(0, taskRepository.markCompletedCalls)
    }

    @Test
    fun `completing a task that does not exist returns an error`() = runTest {
        val taskRepository = FakeTaskRepository(initialTask = null)
        val useCase = CompleteTaskUseCase(taskRepository)

        val result = useCase("missing-task")

        assertTrue(result is Result.Error)
        assertEquals(0, taskRepository.markCompletedCalls)
    }
}
