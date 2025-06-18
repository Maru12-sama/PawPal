package com.example.pawpal.signInUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pawpal.databinding.FragmentSignInUpBinding
import com.google.android.material.tabs.TabLayoutMediator

class SignInUpFragment : Fragment() {

    private var _binding: FragmentSignInUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SignInUpPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SignInUpPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPagerSign.adapter = adapter

        val tabTitles = arrayOf("Sign In", "Sign Up")

        TabLayoutMediator(binding.TabLayout, binding.viewPagerSign) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
