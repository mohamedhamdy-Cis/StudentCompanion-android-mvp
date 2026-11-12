package com.studentcompanion.app.data.repository.rest

import android.net.Uri
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.core.util.safeCall
import com.studentcompanion.app.data.remote.rest.RestDataRefreshBus
import com.studentcompanion.app.data.remote.rest.S3UploadDataSource
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.CompleteTaskRequest
import com.studentcompanion.app.data.remote.rest.dto.SubmitHomeworkRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class RestTaskRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
    private val uploadDataSource: S3UploadDataSource,
    private val refreshBus: RestDataRefreshBus,
) : TaskRepository {

    override fun getDailyTasks(studentId: String): Flow<List<Task>> =
        refreshBus.changes.onStart { emit(Unit) }
            .map { api.getTasks().map { it.toDomain() } }
            .catch { emit(emptyList()) }

    override fun getTask(taskId: String): Flow<Task?> =
        refreshBus.changes.onStart { emit(Unit) }
            .map { runCatching { api.getTask(taskId).toDomain() }.getOrNull() }

    override suspend fun ensureTodaysPrayerTasks(studentId: String): Result<Unit> =
        safeCall("Couldn't set up today's prayers") {
            api.ensureTodaysPrayerTasks()
            Unit
        }.also { if (it is Result.Success) refreshBus.notifyChanged() }

    override suspend fun markTaskCompleted(taskId: String, comment: String?): Result<Unit> =
        safeCall("Couldn't update the task") {
            api.completeTask(taskId, CompleteTaskRequest(comment))
            Unit
        }.also { if (it is Result.Success) refreshBus.notifyChanged() } // also affects points — see RestDataRefreshBus

    // Each image is presigned and PUT directly to object storage (never
    // through our own API server) before the submit call below records its
    // metadata — see S3UploadDataSource and docs/API_CONTRACT.md.
    override suspend fun submitHomework(
        taskId: String,
        comment: String?,
        imageUris: List<Uri>,
    ): Result<Unit> = safeCall("Couldn't submit homework") {
        val attachments = uploadDataSource.uploadHomeworkImages(taskId, imageUris)
        api.submitHomework(taskId, SubmitHomeworkRequest(comment, attachments))
        Unit
    }.also { if (it is Result.Success) refreshBus.notifyChanged() }
}
