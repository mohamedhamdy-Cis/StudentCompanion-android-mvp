package com.studentcompanion.app.data.repository.rest

import android.net.Uri
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.remote.rest.S3UploadDataSource
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.CompleteTaskRequest
import com.studentcompanion.app.data.remote.rest.dto.SubmitHomeworkRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RestTaskRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
    private val uploadDataSource: S3UploadDataSource,
) : TaskRepository {

    override fun getDailyTasks(studentId: String): Flow<List<Task>> = flow {
        emit(api.getTasks().map { it.toDomain() })
    }

    override fun getTask(taskId: String): Flow<Task?> = flow {
        emit(runCatching { api.getTask(taskId).toDomain() }.getOrNull())
    }

    override suspend fun ensureTodaysPrayerTasks(studentId: String): Result<Unit> = try {
        api.ensureTodaysPrayerTasks()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't set up today's prayers", e)
    }

    override suspend fun markTaskCompleted(taskId: String, comment: String?): Result<Unit> = try {
        api.completeTask(taskId, CompleteTaskRequest(comment))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't update the task", e)
    }

    // Each image is presigned and PUT directly to object storage (never
    // through our own API server) before the submit call below records its
    // metadata — see S3UploadDataSource and docs/API_CONTRACT.md.
    override suspend fun submitHomework(
        taskId: String,
        comment: String?,
        imageUris: List<Uri>,
    ): Result<Unit> = try {
        val attachments = uploadDataSource.uploadHomeworkImages(taskId, imageUris)
        api.submitHomework(taskId, SubmitHomeworkRequest(comment, attachments))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't submit homework", e)
    }
}
