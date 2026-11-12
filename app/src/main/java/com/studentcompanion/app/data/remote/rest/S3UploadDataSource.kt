package com.studentcompanion.app.data.remote.rest

import android.net.Uri
import com.studentcompanion.app.core.util.ImageCompressor
import com.studentcompanion.app.data.remote.rest.dto.PresignUploadRequest
import com.studentcompanion.app.data.remote.rest.dto.SubmitHomeworkAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

private const val JPEG_CONTENT_TYPE = "image/jpeg"

/**
 * Compresses a homework image, asks the backend for a presigned PUT URL,
 * uploads directly to object storage (S3/R2 — never through our own API
 * server), and returns the metadata [submitHomework][StudentCompanionApi.submitHomework]
 * needs. Mirrors FirebaseStorageDataSource's role for the REST backend.
 */
@Singleton
class S3UploadDataSource @Inject constructor(
    private val api: StudentCompanionApi,
    private val imageCompressor: ImageCompressor,
    @PlainApi private val uploadClient: OkHttpClient, // no Authorization header needed for a presigned URL
) {
    suspend fun uploadHomeworkImage(taskId: String, uri: Uri): SubmitHomeworkAttachment =
        withContext(Dispatchers.Default) {
            val bytes = imageCompressor.compress(uri)
            val presigned = api.presignUpload(
                taskId,
                PresignUploadRequest(contentType = JPEG_CONTENT_TYPE, sizeBytes = bytes.size.toLong()),
            )

            val request = Request.Builder()
                .url(presigned.uploadUrl)
                .put(bytes.toRequestBody(JPEG_CONTENT_TYPE.toMediaType()))
                .build()
            uploadClient.newCall(request).execute().use { response ->
                require(response.isSuccessful) { "Upload failed: HTTP ${response.code}" }
            }

            SubmitHomeworkAttachment(url = presigned.publicUrl, sizeBytes = bytes.size.toLong())
        }

    suspend fun uploadHomeworkImages(taskId: String, uris: List<Uri>): List<SubmitHomeworkAttachment> =
        uris.map { uploadHomeworkImage(taskId, it) }
}
