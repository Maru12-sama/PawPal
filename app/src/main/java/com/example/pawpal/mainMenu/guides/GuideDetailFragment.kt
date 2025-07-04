package com.example.pawpal.mainMenu.guides

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawpal.R

class GuideDetailFragment : Fragment() {

    companion object {
        private const val TAG = "GuideDetailFragment"
    }

    private lateinit var titleText: TextView
    private lateinit var dateText: TextView
    private lateinit var sourceText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var imageView: ImageView
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "GuideDetailFragment onCreateView called")
        val view = inflater.inflate(R.layout.fragment_guide_detail, container, false)

        // Initialize views
        titleText = view.findViewById(R.id.detailTitle)
        dateText = view.findViewById(R.id.detailDate)
        sourceText = view.findViewById(R.id.detailSource)
        descriptionText = view.findViewById(R.id.detailDescription)
        imageView = view.findViewById(R.id.detailImage)
        backButton = view.findViewById(R.id.backButton)

        // Set up back button
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Load data from arguments
        loadDataFromArguments()

        return view
    }

    private fun loadDataFromArguments() {
        arguments?.let { args ->
            try {
                val title = args.getString("title") ?: "No title"
                val date = args.getString("date") ?: "No date"
                val source = args.getString("source") ?: "No source"
                val description = args.getString("description") ?: "No description available."
                val imageRes = args.getInt("imageRes", R.drawable.test)

                titleText.text = title
                dateText.text = date
                sourceText.text = "Source: $source"
                descriptionText.text = description
                imageView.setImageResource(imageRes)

                Log.d(TAG, "Data loaded successfully - Title: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing arguments: ${e.message}")
                showDefaultContent()
            }
        } ?: run {
            Log.e(TAG, "Arguments are null")
            showDefaultContent()
        }
    }

    private fun showDefaultContent() {
        titleText.text = "Guide tidak ditemukan"
        dateText.text = "No date"
        sourceText.text = "No source"
        descriptionText.text = "Konten guide tidak tersedia."
        imageView.setImageResource(R.drawable.test)
    }
}