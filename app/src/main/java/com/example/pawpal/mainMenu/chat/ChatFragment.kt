package com.example.pawpal.mainMenu.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.pawpal.R
import com.example.pawpal.mainMenu.chat.chatbot.ChatBotFragment
import com.example.pawpal.mainMenu.chat.faq.FAQFragment

class ChatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        val chatBotCard: LinearLayout = view.findViewById(R.id.cardChatBot)
        val faqCard: LinearLayout = view.findViewById(R.id.cardFAQ)

        chatBotCard.setOnClickListener {
            val fragment = ChatBotFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        faqCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FAQFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
