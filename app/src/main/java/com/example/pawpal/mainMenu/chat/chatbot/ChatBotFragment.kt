package com.example.pawpal.mainMenu.chat.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpal.AppDatabaseHelper
import com.example.pawpal.ChatMessage
import com.example.pawpal.R
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatBotFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var dbHelper: AppDatabaseHelper
    private lateinit var messageInput: EditText
    private lateinit var messages: MutableList<ChatMessage>

    // API Key Gemini – Ganti dengan milikmu sendiri
    private val geminiApiKey = "AIzaSyBr6NAPFBzpaJPiZgJf6_1iPNqURNj5UO4"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_chat_bot, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewChat)
        messageInput = view.findViewById(R.id.editMessage)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

        dbHelper = AppDatabaseHelper(requireContext())
        messages = dbHelper.getAllMessages().toMutableList()
        adapter = ChatAdapter(messages)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val input = messageInput.text.toString().trim()
            if (input.isNotEmpty()) {
                sendMessage(input)
                messageInput.setText("")
            }
        }

        return view
    }

    private fun sendMessage(userInput: String) {
        val timestamp = getCurrentTimestamp()
        val userMsg = ChatMessage(message = userInput, isUser = true, timestamp = timestamp)

        messages.add(userMsg)
        dbHelper.insertMessage(userMsg)
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)

        getBotResponse(userInput)
    }

    private fun getBotResponse(userInput: String) {
        val timestamp = getCurrentTimestamp()

        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = geminiApiKey
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = generativeModel.generateContent(
                    content { text(userInput) }
                )

                val botText = response.text ?: "Maaf, saya tidak dapat menjawab saat ini."
                val botMsg = ChatMessage(message = botText.trim(), isUser = false, timestamp = timestamp)

                messages.add(botMsg)
                dbHelper.insertMessage(botMsg)
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}
