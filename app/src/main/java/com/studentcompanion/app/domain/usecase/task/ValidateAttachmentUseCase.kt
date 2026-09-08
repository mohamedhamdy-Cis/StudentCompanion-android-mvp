package com.studentcompanion.app.domain.usecase.task

import android.content.ContentResolver
import android.net.Uri
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.Result
import javax.inject.Inject

/**
 * Enforces the 5 MB per-image limit for homework attachments *before* any
 * upload starts, so the student gets instant feedback instead of a failed
 * network call.
 */
class ValidateAttachmentUseCase @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    operator fun invoke(uri: Uri): Result<Unit> {
        val sizeBytes = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (sizeBytes < 0) return Result.Error("Couldn't read the selected image")
        if (sizeBytes > Constants.MAX_ATTACHMENT_SIZE_BYTES) {
            val sizeMb = sizeBytes / (1024.0 * 1024.0)
            return Result.Error("Image is %.1f MB — max allowed size is 5 MB".format(sizeMb))
        }
        return Result.Success(Unit)
    }
}
