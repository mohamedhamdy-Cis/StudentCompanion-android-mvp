package com.studentcompanion.app.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.mapper.toDomain
import com.studentcompanion.app.data.model.TaskDto
import com.studentcompanion.app.data.remote.firebase.FirebaseStorageDataSource
import com.studentcompanion.app.data.remote.firebase.FirestoreTaskDataSource
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDataSource: FirestoreTaskDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
) : TaskRepository {

    override fun getDailyTasks(studentId: String): Flow<List<Task>> =
        taskDataSource.observeDailyTasks(studentId).map { list ->
            list.map { it.toDomain() }.sortedBy { it.dueAt }
        }

    override fun getTask(taskId: String): Flow<Task?> =
        taskDataSource.observeTask(taskId).map { it?.toDomain() }

    override suspend fun ensureTodaysPrayerTasks(studentId: String): Result<Unit> = try {
        val zone = ZoneId.of("Asia/Riyadh")
        val today = LocalDate.now(zone)
        val existing = taskDataSource.observeDailyTasks(studentId).first()
            .map { it.toDomain() }
            .filter { it.type.name == "PRAYER" }
            .filter { it.dueAt.atZone(zone).toLocalDate() == today }
        if (existing.isEmpty()) {
            DefaultPrayers.forEach { prayer ->
                val dueAt = today.atTime(prayer.time).atZone(zone).toInstant()
                taskDataSource.createTask(
                    TaskDto(
                        studentId = studentId,
                        type = "PRAYER",
                        title = prayer.title,
                        description = "Mark ${prayer.title} as done when you have prayed.",
                        dueAt = Timestamp(Date.from(dueAt)),
                        status = "PENDING",
                        pointsValue = Constants.POINTS_PRAYER_ON_TIME,
                    )
                )
            }
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't set up today's prayers", e)
    }

    override suspend fun markTaskCompleted(taskId: String, comment: String?): Result<Unit> = try {
        taskDataSource.markCompleted(taskId, comment)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't update the task", e)
    }

    override suspend fun submitHomework(taskId: String, comment: String?, imageUris: List<Uri>): Result<Unit> = try {
        val attachments = storageDataSource.uploadHomeworkImages(taskId, imageUris)
        taskDataSource.submitHomework(taskId, comment, attachments)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't submit homework", e)
    }

    private data class PrayerSeed(val title: String, val time: LocalTime)

    private companion object {
        val DefaultPrayers = listOf(
            PrayerSeed("Fajr", LocalTime.of(5, 0)),
            PrayerSeed("Dhuhr", LocalTime.of(12, 15)),
            PrayerSeed("Asr", LocalTime.of(15, 30)),
            PrayerSeed("Maghrib", LocalTime.of(18, 15)),
            PrayerSeed("Isha", LocalTime.of(19, 45)),
        )
    }
}
