package com.example.pawpal.signInUp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pawpal.signInUp.screens.SignInFragment
import com.example.pawpal.signInUp.screens.SignUpFragment

class SignInUpPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2 // Misalnya hanya ada dua halaman: SignIn dan SignUp
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SignInFragment()
            1 -> SignUpFragment()
            else -> SignInFragment() // fallback
        }
    }
}
