package com.studentcompanion.app.domain.usecase.task

import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.testutil.FakePointsRepository
import com.studentcompanion.app.testutil.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class CompleteTaskUseCaseTest {

    private fun prayerTask(dueAt: Instant, status: TaskStatus = TaskStatus.PENDING) = Task(
        id = "task-1",
        studentId = "student-1",
        type = TaskType.PRAYER,
        title = "Fajr",
        dueAt = dueAt,
        status = status,
        pointsValue = Constants.POINTS_PRAYER_ON_TIME,
    )

    @Test
    fun `completing a prayer within an hour of its due time awards the on-time points`() = runTest {
        val task = prayerTask(dueAt = Instant.now().minus(10, ChronoUnit.MINUTES))
        val taskRepository = FakeTaskRepository(task)
        val pointsRepository = FakePointsRepository()
        val useCase = CompleteTaskUseCase(taskRepository, pointsRepository)

        val result = useCase(task.id)

        assertTrue(result is Result.Success)
        assertEquals(1, pointsRepository.awarded.size)
        assertEquals(Constants.POINTS_PRAYER_ON_TIME, pointsRepository.awarded.first().points)
    }

    @Test
    fun `completing a prayer more than an hour late awards the late points`() = runTest {
        val task = prayerTask(dueAt = Instant.now().minus(2, ChronoUnit.HOURS))
        val taskRepository = FakeTaskRepository(task)
        val pointsRepository = FakePointsRepository()
        val useCase = CompleteTaskUseCase(taskRepository, pointsRepository)

        useCase(task.id)

        assertEquals(Constants.POINTS_PRAYER_LATE, pointsRepository.awarded.first().points)
    }

    @Test
    fun `completing an already-completed task returns an error and awards nothing`() = runTest {
        val task = prayerTask(dueAt = Instant.now(), status = TaskStatus.APPROVED)
        val taskRepository = FakeTaskRepository(task)
        val pointsRepository = FakePointsRepository()
        val useCase = CompleteTaskUseCase(taskRepository, pointsRepository)

        val result = useCase(task.id)

        assertTrue(result is Result.Error)
        assertTrue(pointsRepository.awarded.isEmpty())
        assertEquals(0, taskRepository.markCompletedCalls)
    }

    @Test
    fun `completing a task that does not exist returns an error`() = runTest {
        val taskRepository = FakeTaskRepository(initialTask = null)
        val pointsRepository = FakePointsRepository()
        val useCase = CompleteTaskUseCase(taskRepository, pointsRepository)

        val result = useCase("missing-task")

        assertTrue(result is Result.Error)
        assertTrue(pointsRepository.awarded.isEmpty())
    }
}
