package com.example.pawpal.mainMenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pawpal.R
import com.example.pawpal.databinding.FragmentMainMenuBinding
import com.example.pawpal.mainMenu.chat.ChatFragment
import com.example.pawpal.mainMenu.chat.chatbot.ChatBotFragment
import com.example.pawpal.mainMenu.guides.GuidesFragment

class MainMenuFragment : Fragment() {

    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)

        // Set default fragment
        loadFragment(HomeFragment())

        // Handle bottom navigation click
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_pets -> loadFragment(PetsFragment())
                R.id.nav_guides -> loadFragment(GuidesFragment())
                R.id.nav_chat -> loadFragment(ChatFragment())
            }
            true
        }

        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        binding.header.visibility = when (fragment) {
            is GuidesFragment -> View.GONE
            is ChatFragment -> View.GONE // Fragment baru ditambahkan
            else -> View.VISIBLE
        }

        binding.bottomNavigation.visibility = if(fragment is ChatBotFragment) View.GONE else
            View.VISIBLE
        // Ganti fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
