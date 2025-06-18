package com.example.pawpal.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.pawpal.R
import com.example.pawpal.onboarding.screens.FirstScreen
import com.example.pawpal.onboarding.screens.SecondScreen
import com.example.pawpal.onboarding.screens.ThirdScreen
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class ViewPagerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        val fragmentList = arrayListOf<Fragment>(
            FirstScreen(),
            SecondScreen(),
            ThirdScreen()
        )

        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        viewPager.adapter = adapter
        val dotsIndicator = view.findViewById<DotsIndicator>(R.id.dots_indicator)
        dotsIndicator.attachTo(viewPager)
        return view
    }

}