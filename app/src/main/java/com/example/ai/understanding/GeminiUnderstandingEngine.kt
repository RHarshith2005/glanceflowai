package com.example.ai.understanding

import android.content.Context
import com.example.BuildConfig
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HybridAIUnderstandingEngine(context: Context) : AIUnderstandingEngine {

    val gemmaLocalEngine = GemmaLocalEngine(context)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    override suspend fun processText(
        rawText: String,
        sourceType: SourceType,
        engineMode: AIEngineMode
    ): ParsedGlance = withContext(Dispatchers.IO) {
        when (engineMode) {
            AIEngineMode.GEMMA_LOCAL -> {
                // Primary: On-Device Google Gemma 2B-IT Neural Inference
                return@withContext gemmaLocalEngine.parseWithGemma(rawText, sourceType)
            }

            AIEngineMode.OFFLINE_NLP -> {
                // Secondary: Rule-based heuristic pattern extractor
                return@withContext OfflineUnderstandingEngine.parse(rawText, sourceType)
            }

            AIEngineMode.CLOUD_GEMINI -> {
                val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

                if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    try {
                        val response = callGeminiApi(rawText, apiKey)
                        if (response != null) {
                            return@withContext response.copy(sourceType = sourceType, rawText = rawText)
                        }
                    } catch (e: Exception) {
                        // Cascade to Gemma Local Engine
                    }
                }

                // Fallback to On-Device Gemma
                return@withContext gemmaLocalEngine.parseWithGemma(rawText, sourceType)
            }
        }
    }

    private fun callGeminiApi(rawText: String, apiKey: String): ParsedGlance? {
        val prompt = """
            You are GlanceFlow AI on an iQOO smartphone. Convert this unstructured raw text captured from the real world into structured JSON.
            Text:
            $rawText

            Respond with strictly valid JSON matching this schema:
            {
              "title": "Short concise title (max 6 words)",
              "category": "Assignment | Task | Reminder | Event | Meeting | Exam | Shopping | Note | Important | General",
              "priority": "HIGH | MEDIUM | LOW",
              "deadline": "e.g. Friday or Tomorrow at 10 AM, or null",
              "tasks": ["specific actionable items extracted from the text"],
              "entities": ["technologies, datasets, or key entities mentioned"],
              "tags": ["#Tag1", "#Tag2"],
              "notes": ["extra context details"]
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)

            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val responseJson = JSONObject(responseBody)
        val textResponse = responseJson
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        val cleanJson = textResponse
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val parsedObj = JSONObject(cleanJson)

        val title = parsedObj.optString("title", "Captured Note")
        val categoryStr = parsedObj.optString("category", "General")
        val priorityStr = parsedObj.optString("priority", "MEDIUM")
        val deadline = if (parsedObj.has("deadline") && !parsedObj.isNull("deadline")) parsedObj.optString("deadline") else null

        val category = try {
            GlanceCategory.valueOf(categoryStr.uppercase())
        } catch (e: Exception) {
            GlanceCategory.GENERAL
        }

        val priority = try {
            GlancePriority.valueOf(priorityStr.uppercase())
        } catch (e: Exception) {
            GlancePriority.MEDIUM
        }

        val tasksList = mutableListOf<String>()
        val tasksArr = parsedObj.optJSONArray("tasks")
        if (tasksArr != null) {
            for (i in 0 until tasksArr.length()) {
                tasksList.add(tasksArr.getString(i))
            }
        }

        val entitiesList = mutableListOf<String>()
        val entitiesArr = parsedObj.optJSONArray("entities")
        if (entitiesArr != null) {
            for (i in 0 until entitiesArr.length()) {
                entitiesList.add(entitiesArr.getString(i))
            }
        }

        val tagsList = mutableListOf<String>()
        val tagsArr = parsedObj.optJSONArray("tags")
        if (tagsArr != null) {
            for (i in 0 until tagsArr.length()) {
                tagsList.add(tagsArr.getString(i))
            }
        }

        val notesList = mutableListOf<String>()
        val notesArr = parsedObj.optJSONArray("notes")
        if (notesArr != null) {
            for (i in 0 until notesArr.length()) {
                notesList.add(notesArr.getString(i))
            }
        }

        return ParsedGlance(
            title = title,
            category = category,
            priority = priority,
            deadline = deadline,
            tasks = tasksList,
            entities = entitiesList,
            tags = tagsList,
            notes = notesList,
            engineUsed = "Cloud Gemini 3.5-Flash"
        )
    }
}
