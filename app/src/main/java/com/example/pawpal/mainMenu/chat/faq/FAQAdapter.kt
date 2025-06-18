package com.example.pawpal.mainMenu.chat.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpal.R

class FAQAdapter(private val list: List<FAQItem>) :
    RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtQuestion: TextView = itemView.findViewById(R.id.txtQuestion)
        val txtAnswer: TextView = itemView.findViewById(R.id.txtAnswer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val faq = list[position]
        holder.txtQuestion.text = faq.question
        holder.txtAnswer.text = faq.answer
    }

    override fun getItemCount(): Int = list.size
}
