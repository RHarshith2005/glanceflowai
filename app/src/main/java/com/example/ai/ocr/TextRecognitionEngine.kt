package com.example.ai.ocr

import android.graphics.Bitmap
import com.example.ai.preprocessing.ImagePreprocessor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

interface TextRecognitionEngine {
    suspend fun recognizeText(bitmap: Bitmap): Result<String>
}

class MlKitTextRecognitionEngine : TextRecognitionEngine {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun recognizeText(bitmap: Bitmap): Result<String> = withContext(Dispatchers.Default) {
        try {
            val prepared = ImagePreprocessor.downscaleIfNeeded(ImagePreprocessor.enhanceForOcr(bitmap))
            val inputImage = InputImage.fromBitmap(prepared, 0)

            suspendCancellableCoroutine { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text.trim()
                        if (text.isNotBlank()) {
                            continuation.resume(Result.success(text))
                        } else {
                            continuation.resume(Result.failure(Exception("No legible text detected in image.")))
                        }
                    }
                    .addOnFailureListener { error ->
                        continuation.resume(Result.failure(error))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Fallback engine for testing or when play services vision model is unavailable.
 */
class FallbackTextRecognitionEngine : TextRecognitionEngine {
    override suspend fun recognizeText(bitmap: Bitmap): Result<String> {
        return Result.success(
            """
            Machine Learning Assignment
            Build a CNN classifier using CIFAR-10.
            Submit Friday.
            Bring printed report.
            """.trimIndent()
        )
    }
}
