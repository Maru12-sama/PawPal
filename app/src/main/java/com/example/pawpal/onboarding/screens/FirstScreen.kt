package com.example.pawpal.onboarding.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.pawpal.R

class FirstScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton: LinearLayout = view.findViewById(R.id.nextButton)
        val skipButton: TextView = view.findViewById(R.id.skipButton)

        val viewPager = parentFragment?.view?.findViewById<ViewPager2>(R.id.viewPager)
            ?: activity?.findViewById<ViewPager2>(R.id.viewPager)

        nextButton.setOnClickListener {
            viewPager?.currentItem = 1
        }

        skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_signInUpFragment)
            onBoardingFinished()
        }
    }

    private fun onBoardingFinished(){
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }
}