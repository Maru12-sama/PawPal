package com.example.pawpal.signInUp.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.pawpal.AppDatabaseHelper
import com.example.pawpal.R
import com.example.pawpal.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signInButton.setOnClickListener {
            val identifier = binding.emailEditText.text.toString()
            val inputPassword = binding.passwordEditText.text.toString()

            val dbHelper = AppDatabaseHelper(requireContext())
            val ownerId = dbHelper.getOwnerId(identifier, inputPassword)

            if (ownerId != null) {
                Toast.makeText(requireContext(), "Sign in successful!", Toast.LENGTH_SHORT).show()

                // Ambil nama owner dari database
                val ownerName = dbHelper.getOwnerName(ownerId)

                // Simpan data ke SharedPreferences
                val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putInt("owner_id", ownerId)
                    putString("owner_name", ownerName ?: "")
                    putBoolean("is_logged_in", true)
                    apply()
                }

                findNavController().navigate(R.id.action_signInUpFragment_to_homeFragment)
                signInFinished()
            } else {
                Toast.makeText(requireContext(), "Username/Email/Phone atau password salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInFinished() {
        val sharedPref = requireActivity().getSharedPreferences("signIn", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}