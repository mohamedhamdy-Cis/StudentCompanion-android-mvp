package com.studentcompanion.app.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.ImageCompressor
import com.studentcompanion.app.data.model.TaskAttachmentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Uploads homework images to Firebase Storage, compressed via [ImageCompressor]. */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
    private val imageCompressor: ImageCompressor,
) {
    suspend fun uploadHomeworkImage(taskId: String, uri: Uri): TaskAttachmentDto {
        val compressed = withContext(Dispatchers.Default) { imageCompressor.compress(uri) }
        require(compressed.size <= Constants.MAX_ATTACHMENT_SIZE_BYTES) {
            "Image is still over 5 MB after compression"
        }

        val fileId = UUID.randomUUID().toString()
        val ref = storage.reference
            .child(Constants.STORAGE_HOMEWORK_ATTACHMENTS)
            .child(taskId)
            .child("$fileId.jpg")

        ref.putBytes(compressed).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        return TaskAttachmentDto(id = fileId, url = downloadUrl, sizeBytes = compressed.size.toLong())
    }

    suspend fun uploadHomeworkImages(taskId: String, uris: List<Uri>): List<TaskAttachmentDto> =
        uris.map { uploadHomeworkImage(taskId, it) }
}
