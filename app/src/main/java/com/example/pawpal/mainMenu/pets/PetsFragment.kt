package com.example.pawpal.mainMenu.pets

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpal.AppDatabaseHelper
import com.example.pawpal.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PetsFragment : Fragment() {

    private var _dbHelper: AppDatabaseHelper? = null
    private val dbHelper: AppDatabaseHelper
        get() = _dbHelper ?: AppDatabaseHelper(requireContext()).also { _dbHelper = it }

    private lateinit var recyclerView: RecyclerView
    private lateinit var singlePetLayout: LinearLayout
    private lateinit var multiplePetsLayout: LinearLayout
    private lateinit var addPetButton: FloatingActionButton
    private lateinit var addPetFab: FloatingActionButton

    private var ownerId: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (_dbHelper == null) {
            _dbHelper = AppDatabaseHelper(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        ownerId = sharedPref.getInt("owner_id", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pets, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.pets_recycler_view)
        singlePetLayout = view.findViewById(R.id.single_pet_layout)
        multiplePetsLayout = view.findViewById(R.id.multiple_pets_layout)
        addPetButton = view.findViewById(R.id.add_pet_button)
        addPetFab = view.findViewById(R.id.add_pet_fab)

        setupViews()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadPets()
    }

    private fun setupViews() {
        // Setup click listeners for add pet buttons
        addPetButton.setOnClickListener {
            navigateToAddPet()
        }

        addPetFab.setOnClickListener {
            navigateToAddPet()
        }
    }

    private fun navigateToAddPet() {
        val addPetFragment = AddPetFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, addPetFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadPets() {
        if (ownerId != null && ownerId!! > 0) {
            val pets = getAllPetsForOwner(ownerId!!)

            when (pets.size) {
                0 -> {
                    // No pets - show empty state or add pet prompt
                    showMultiplePetsView(emptyList())
                }
                1 -> {
                    // Single pet - show detailed view
                    showSinglePetView(pets[0])
                }
                else -> {
                    // Multiple pets - show list view
                    showMultiplePetsView(pets)
                }
            }
        }
    }

    private fun showSinglePetView(pet: Pet) {
        singlePetLayout.visibility = View.VISIBLE
        multiplePetsLayout.visibility = View.GONE
        addPetButton.visibility = View.VISIBLE

        // Populate single pet view
        val petAvatar = singlePetLayout.findViewById<CircleImageView>(R.id.pet_avatar)
        val petNameDisplay = singlePetLayout.findViewById<TextView>(R.id.pet_name_display)
        val petAgeDisplay = singlePetLayout.findViewById<TextView>(R.id.pet_age_display)
        val petTypeBreeDisplay = singlePetLayout.findViewById<TextView>(R.id.pet_type_breed_display)
        val addRemindersButton = singlePetLayout.findViewById<Button>(R.id.add_reminders_button)
        val memoriesButton = singlePetLayout.findViewById<Button>(R.id.memories_button)
        val remindersText = singlePetLayout.findViewById<TextView>(R.id.reminders_text)

        // Set pet data
        petNameDisplay.text = pet.name
        petAgeDisplay.text = "${pet.age} Year${if (pet.age != 1) "s" else ""} Old"
        petTypeBreeDisplay.text = "${pet.sex} ${pet.type}\n${pet.breed}"

        // Load pet photo
        loadPetPhoto(petAvatar, pet.photoPath)

        // Setup button listeners
        addRemindersButton.setOnClickListener {
            val addReminderFragment = AddReminderFragment.newInstance(pet.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, addReminderFragment)
                .addToBackStack(null)
                .commit()
        }

        memoriesButton.setOnClickListener {
            // Handle memories - navigate to memories fragment
            // val memoriesFragment = MemoriesFragment.newInstance(pet.id)
            // parentFragmentManager.beginTransaction()
            //     .replace(R.id.frame_container, memoriesFragment)
            //     .addToBackStack(null)
            //     .commit()
        }

        // Add long click listener for editing pet
        singlePetLayout.setOnLongClickListener {
            val editPetFragment = EditPetFragment.newInstance(pet.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, editPetFragment)
                .addToBackStack(null)
                .commit()
            true
        }

        // Load reminders for this pet
        loadRemindersForPet(pet.id, remindersText)
    }

    private fun showMultiplePetsView(pets: List<Pet>) {
        singlePetLayout.visibility = View.GONE
        multiplePetsLayout.visibility = View.VISIBLE
        addPetButton.visibility = View.GONE

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = PetAdapter(
            pets = pets,
            onItemClick = { pet ->
                // When pet is clicked, show detail fragment
                val detailFragment = PetDetailFragment.newInstance(pet)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { pet ->
                // Long click for editing
                val editPetFragment = EditPetFragment.newInstance(pet.id)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, editPetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun loadPetPhoto(imageView: CircleImageView,photoPath: String?) {
        if (photoPath != null && File(photoPath).exists()) {
            imageView.setImageURI(Uri.fromFile(File(photoPath)))
        } else {
            imageView.setImageResource(R.drawable.ic_pet_placeholder)
        }
    }

    private fun getAllPetsForOwner(ownerId: Int): List<Pet> {
        val db = dbHelper.readableDatabase
        val pets = mutableListOf<Pet>()
        val cursor = db.rawQuery(
            "SELECT id, name, type, breed, dob, sex, photo_path FROM pet WHERE owner_id = ?",
            arrayOf(ownerId.toString())
        )
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: ""
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type")) ?: ""
            val breed = cursor.getString(cursor.getColumnIndexOrThrow("breed")) ?: ""
            val sex = cursor.getString(cursor.getColumnIndexOrThrow("sex")) ?: ""
            val dob = cursor.getString(cursor.getColumnIndexOrThrow("dob")) ?: ""
            val photoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"))
            val age = calculateAge(dob, formatter)
            pets.add(Pet(id, name, type, age, breed, sex, photoPath))
        }
        cursor.close()
        return pets
    }

    private fun calculateAge(dob: String, formatter: SimpleDateFormat): Int {
        return try {
            val date = formatter.parse(dob)
            val cal = Calendar.getInstance()
            val now = cal.time
            cal.time = date!!
            val diff = now.time - cal.timeInMillis
            val years = (diff / (1000L * 60 * 60 * 24 * 365)).toInt()
            maxOf(0, years) // Ensure non-negative age
        } catch (e: Exception) {
            0 // Default to 0 if parsing fails
        }
    }

    private fun loadRemindersForPet(petId: Int, remindersTextView: TextView) {
        // Load reminders from database
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) as count FROM reminders WHERE pet_id = ?",  // Removed is_active check since it doesn't exist in schema
            arrayOf(petId.toString())
        )

        var reminderCount = 0
        if (cursor.moveToFirst()) {
            reminderCount = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
        }
        cursor.close()

        remindersTextView.text = if (reminderCount > 0) {
            "$reminderCount reminder${if (reminderCount > 1) "s" else ""}"
        } else {
            "Empty"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _dbHelper = null // Clean up to avoid memory leaks
    }
}