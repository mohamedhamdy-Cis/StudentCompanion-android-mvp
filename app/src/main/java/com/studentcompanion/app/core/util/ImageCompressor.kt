package com.studentcompanion.app.core.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downscales and re-encodes an image as JPEG before upload — a phone camera
 * photo is routinely 10-20 MB uncompressed, well past the 5 MB attachment
 * limit, and raw uploads cost real bandwidth for no visible benefit on a
 * phone screen.
 */
@Singleton
class ImageCompressor @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    fun compress(uri: Uri): ByteArray {
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
