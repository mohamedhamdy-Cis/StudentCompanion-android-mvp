package com.studentcompanion.app.domain.repository

import android.net.Uri
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getDailyTasks(studentId: String): Flow<List<Task>>
    fun getTask(taskId: String): Flow<Task?>

    suspend fun ensureTodaysPrayerTasks(studentId: String): Result<Unit>

    suspend fun markTaskCompleted(taskId: String, comment: String? = null): Result<Unit>

    /**
     * Uploads homework attachment(s) then marks the task submitted.
     * [imageUris] must already be validated against the 5 MB per-image
     * limit by the caller (see ValidateAttachmentUseCase).
     */
    suspend fun submitHomework(taskId: String, comment: String?, imageUris: List<Uri>): Result<Unit>
}
