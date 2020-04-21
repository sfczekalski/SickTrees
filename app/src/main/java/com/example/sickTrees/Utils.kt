package com.example.sickTrees

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Utils {
    fun assetFilePath(
        context: Context,
        assetName: String
    ): String? {
        val file = File(context.filesDir, assetName)
        try {
            context.assets.open(assetName).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (`is`.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
                return file.absolutePath
            }
        } catch (e: IOException) {
            Log.e(
                "pytorchandroid",
                "Error process asset $assetName to file path"
            )
        }
        return null
    }

    fun compressResultImg(photoPath: String): Bitmap? {
        // Target dimensions
        val targetW: Int = 2048
        val targetH: Int = 1365

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }

        return BitmapFactory.decodeFile(photoPath, bmOptions)
    }
}