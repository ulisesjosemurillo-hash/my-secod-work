package com.example

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class LocalOcrService {
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    suspend fun analyzeImageForRawText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun analyzeImage(bitmap: Bitmap): ReprogramacionData {
        val text = analyzeImageForRawText(bitmap)
        return DataExtractor.parseOcrResult(text)
    }

    suspend fun analyzeCalendarBook(bitmap: Bitmap): List<CalendarEvent> {
        return emptyList()
    }
}
