package com.example.ai.preprocessing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImagePreprocessor {

    /**
     * Enhances blackboard/whiteboard/document photos for higher OCR contrast.
     */
    fun enhanceForOcr(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Grayscale matrix
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)

        // 2. High contrast curve (boost text against blackboard or paper)
        val contrast = 1.35f
        val brightness = -15f
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f + brightness

        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(contrastMatrix)

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return output
    }

    /**
     * Rescales if image is excessively large to avoid OOM while preserving OCR readability.
     */
    fun downscaleIfNeeded(source: Bitmap, maxDimension: Int = 1600): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= maxDimension && height <= maxDimension) return source

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (ratio > 1f) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }
}
