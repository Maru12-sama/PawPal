package com.example.pawpal.signInUp.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.pawpal.AppDatabaseHelper
import com.example.pawpal.R
import com.example.pawpal.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val petTypes = listOf("Cat", "Dog")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, petTypes)
        binding.petTypeDropdown.setAdapter(adapter)

        binding.dobInputLayout.setEndIconOnClickListener {
            showDatePicker()
        }

        binding.dobEditText.setOnClickListener {
            showDatePicker()
        }

        binding.signUpButton.setOnClickListener {
            val petName = binding.petNameEditText.text.toString()
            val petType = binding.petTypeDropdown.text.toString()
            val breed = binding.breedEditText.text.toString()
            val sex = binding.sexEditText.text.toString()
            val dob = binding.dobEditText.text.toString()

            val ownerName = binding.ownerNameEditText.text.toString()
            val ownerPhone = binding.ownerPhoneEditText.text.toString()
            val ownerEmail = binding.ownerEmailEditText.text.toString()

            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val rePassword = binding.reenterPasswordEditText.text.toString()

            val isTermsChecked = binding.termsCheckBox.isChecked

            if (username.isNotBlank() && password == rePassword && isTermsChecked) {
                val sharedPref = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("username", username)
                    putString("password", password)
                    apply()
                }

                val dbHelper = AppDatabaseHelper(requireContext())
                val result = dbHelper.insertOwnerWithPet(
                    ownerName, ownerPhone, ownerEmail, username, password,
                    petName, petType, breed, sex, dob
                )


                val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPagerSign)
                viewPager?.currentItem = 0

                Snackbar.make(binding.root, "Berhasil mendaftar, silakan login", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "Pastikan semua data valid dan checkbox dicentang", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                binding.dobEditText.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
