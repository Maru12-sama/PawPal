package com.example.pawpal.mainMenu

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.pawpal.AppDatabaseHelper
import com.example.pawpal.R

class HomeFragment : Fragment() {

    private lateinit var dbHelper: AppDatabaseHelper
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = AppDatabaseHelper(requireContext())

        // Ambil user ID dari SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("owner_id", -1)

        val noteCard = view.findViewById<CardView>(R.id.noteCard)
        val noteText = view.findViewById<TextView>(R.id.noteText)

        // Ambil nama pet terakhir dari database
        val lastPetName = dbHelper.getLastPetNameForUser(userId)

        if (!lastPetName.isNullOrEmpty()) {
            noteCard.visibility = View.VISIBLE
            noteText.text = "Add Reminders for $lastPetName Now!"
        } else {
            noteCard.visibility = View.GONE
        }
    }
}
