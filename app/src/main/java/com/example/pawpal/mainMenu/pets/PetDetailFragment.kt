package com.example.pawpal.mainMenu.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pawpal.R

class PetDetailFragment : Fragment() {

    companion object {
        fun newInstance(pet: Pet): PetDetailFragment {
            val fragment = PetDetailFragment()
            val args = Bundle()
            args.putParcelable("pet", pet)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pet_detail, container, false)
        val pet = arguments?.getParcelable<Pet>("pet")
        if (pet != null) {
            view.findViewById<TextView>(R.id.pet_name).text = "Name: ${pet.name}"
            view.findViewById<TextView>(R.id.pet_type).text = "Type: ${pet.type}"
            view.findViewById<TextView>(R.id.pet_breed).text = "Breed: ${pet.breed}"
            view.findViewById<TextView>(R.id.pet_sex).text = "Sex: ${pet.sex}" // 'sex' tidak ada di model baru
            view.findViewById<TextView>(R.id.pet_dob).text = "Age: ${pet.age} years"
            view.findViewById<TextView>(R.id.reminders_text).text = "Reminders: Empty"
        }
        return view
    }
}