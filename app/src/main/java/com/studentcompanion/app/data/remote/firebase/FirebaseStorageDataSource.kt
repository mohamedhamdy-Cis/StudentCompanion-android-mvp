package com.studentcompanion.app.data.remote.firebase

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.data.model.TaskAttachmentDto
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads homework images to Firebase Storage. Size is already validated
 * by ValidateAttachmentUseCase before this is called, but we defensively
 * re-check here since this class could later be reused (e.g. from a
 * background sync worker) without going through the use case.
 */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
    private val contentResolver: ContentResolver,
) {
    suspend fun uploadHomeworkImage(taskId: String, uri: Uri): TaskAttachmentDto {
        val sizeBytes = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
        require(sizeBytes <= Constants.MAX_ATTACHMENT_SIZE_BYTES) { "Attachment exceeds 5 MB limit" }

        val fileId = UUID.randomUUID().toString()
        val ref = storage.reference
            .child(Constants.STORAGE_HOMEWORK_ATTACHMENTS)
            .child(taskId)
            .child("$fileId.jpg")

        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        return TaskAttachmentDto(id = fileId, url = downloadUrl, sizeBytes = sizeBytes)
    }

    suspend fun uploadHomeworkImages(taskId: String, uris: List<Uri>): List<TaskAttachmentDto> =
        uris.map { uploadHomeworkImage(taskId, it) }
}
