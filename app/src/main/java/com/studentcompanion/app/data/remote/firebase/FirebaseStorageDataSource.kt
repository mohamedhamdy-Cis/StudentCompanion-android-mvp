package com.studentcompanion.app.data.remote.firebase

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.data.model.TaskAttachmentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads homework images to Firebase Storage. Images are downscaled and
 * re-encoded as JPEG before upload — a phone camera photo is routinely
 * 10-20 MB uncompressed, well past what the 5 MB limit was ever meant to
 * allow through, and raw uploads cost real Storage bandwidth/money for no
 * visible benefit on a phone screen.
 */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
    private val contentResolver: ContentResolver,
) {
    suspend fun uploadHomeworkImage(taskId: String, uri: Uri): TaskAttachmentDto {
        val compressed = withContext(Dispatchers.Default) { compressImage(uri) }
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

    private fun compressImage(uri: Uri): ByteArray {
        val original = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: error("Couldn't read the selected image")
        val scaled = original.scaleToMaxDimension(MAX_DIMENSION_PX)
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        if (scaled !== original) original.recycle()
        scaled.recycle()
        return output.toByteArray()
    }

    private fun Bitmap.scaleToMaxDimension(maxDimension: Int): Bitmap {
        if (width <= maxDimension && height <= maxDimension) return this
        val scale = maxDimension.toFloat() / maxOf(width, height)
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    private companion object {
        const val MAX_DIMENSION_PX = 1600
        const val JPEG_QUALITY = 80
    }
}
