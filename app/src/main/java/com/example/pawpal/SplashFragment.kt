package com.example.pawpal

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use lifecycleScope instead of deprecated Handler
        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)

            when {
                signInFinished() -> {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }
                onBoardingFinished() -> {
                    findNavController().navigate(R.id.action_splashFragment_to_signInUpFragment)
                }
                else -> {
                    findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
                }
            }
        }

    }

    private fun onBoardingFinished(): Boolean{
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished", false)
    }

    private fun signInFinished(): Boolean{
        val sharedPref = requireActivity().getSharedPreferences("signIn", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished", false)
    }
}
