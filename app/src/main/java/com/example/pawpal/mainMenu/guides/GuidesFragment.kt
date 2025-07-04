package com.example.pawpal.mainMenu.guides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpal.R

class GuidesFragment : Fragment() {

    private lateinit var feedingRecycler: RecyclerView
    private lateinit var groomingRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guides, container, false)

        feedingRecycler = view.findViewById(R.id.feedingRecycler)
        groomingRecycler = view.findViewById(R.id.groomingRecycler)

        feedingRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        groomingRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController != null) {
            feedingRecycler.adapter = GuideAdapter(getDummyFeeds(), navController)
            groomingRecycler.adapter = GuideAdapter(getDummyGrooms(), navController)
        }
        return view
    }

    private fun getDummyFeeds(): List<GuideItem> = listOf(
        GuideItem("Apa Saja Makanan yang Menjadi Racun bagi Anjing", "YouTube", "5 Maret 2025", R.drawable.test),
        GuideItem("4 Kebiasaan Makan Kucing yang Tak Biasa", "YouTube", "8 Maret 2025", R.drawable.test),
        GuideItem("Why They Confuse You About What to Feed", "YouTube", "10 Maret 2025", R.drawable.test)
    )

    private fun getDummyGrooms(): List<GuideItem> = listOf(
        GuideItem("Dog Grooming: 10 Beginner MISTAKES", "YouTube", "9 Maret 2025", R.drawable.test),
        GuideItem("The top five dog-grooming tips from pro", "YouTube", "11 Maret 2025", R.drawable.test),
        GuideItem("Tips for brushing your cat", "YouTube", "13 Maret 2025", R.drawable.test)
    )

    fun openSeeMoreFeeding(view: View) {
        // Logika untuk "See More" Feeding Tips
    }

    fun openSeeMoreGrooming(view: View) {
        // Logika untuk "See More" Grooming Tips
    }
}