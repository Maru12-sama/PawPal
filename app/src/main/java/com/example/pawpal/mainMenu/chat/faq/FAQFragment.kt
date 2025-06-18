package com.example.pawpal.mainMenu.chat.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpal.R

class FAQFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FAQAdapter

    private val faqList = listOf(
        FAQItem("Apa itu PawPal?", "PawPal adalah aplikasi perawatan hewan peliharaan."),
        FAQItem("Bagaimana cara menambahkan hewan?", "Masuk ke tab Pets dan tekan tombol +."),
        FAQItem("Apa fungsi ChatBot?", "Untuk konsultasi cepat terkait kondisi hewan."),
        FAQItem("Apakah PawPal gratis?", "Ya, aplikasi ini gratis digunakan.")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_faq, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFAQ)
        adapter = FAQAdapter(faqList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }
}
