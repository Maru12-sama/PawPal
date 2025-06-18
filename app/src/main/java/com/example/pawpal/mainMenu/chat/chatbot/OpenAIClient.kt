package com.example.pawpal.mainMenu.chat.chatbot

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject

object OpenAIClient {

    private val client = OkHttpClient()

    fun getChatResponse(prompt: String): String {
        val mediaType = "application/json".toMediaType()
        val body = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer ${Constants.OPENAI_API_KEY}")
            .post(RequestBody.create(mediaType, body.toString()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return "Failed: ${response.code}"
            val json = JSONObject(response.body?.string() ?: "")
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }
}
