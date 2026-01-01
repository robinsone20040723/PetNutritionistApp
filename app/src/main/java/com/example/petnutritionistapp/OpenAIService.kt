package com.example.petnutritionistapp

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object OpenAIService {

    private const val API_URL = "https://api.openai.com/v1/chat/completions"

    fun requestMealPlan(
        prompt: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val apiKey = BuildConfig.OPENAI_API_KEY

        if (apiKey.isBlank()) {
            onError("API Key 為空，請檢查 local.properties")
            return
        }

        // ===== 正確且保守的 Request JSON =====
        val requestJson = JSONObject().apply {
            put("model", "gpt-4.1-mini")   // ✅ 穩定、不容易 500
            put("temperature", 0.5)
            put("max_tokens", 500)

            val messages = org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "你是一位專業的犬隻營養顧問，請提供非醫療建議。")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }

            put("messages", messages)
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(
                requestJson.toString()
                    .toRequestBody("application/json".toMediaType())
            )
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                onError("連線失敗：${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (!response.isSuccessful || body == null) {
                    onError("API error: ${response.code}")
                    return
                }

                try {
                    val json = JSONObject(body)
                    val result = json
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    onSuccess(result)

                } catch (e: Exception) {
                    onError("解析回應失敗")
                }
            }
        })
    }
}
