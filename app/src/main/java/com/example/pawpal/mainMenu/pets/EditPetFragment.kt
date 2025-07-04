package com.example.pawpal.mainMenu.pets

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.pawpal.AppDatabaseHelper
import com.example.pawpal.R
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditPetFragment : Fragment() {

    private var _dbHelper: AppDatabaseHelper? = null
    private val dbHelper: AppDatabaseHelper
        get() = _dbHelper ?: AppDatabaseHelper(requireContext()).also { _dbHelper = it }

    private lateinit var petAvatar: CircleImageView
    private lateinit var editPhotoButton: ImageButton
    private lateinit var nameEditText: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var breedEditText: EditText
    private lateinit var sexSpinner: Spinner
    private lateinit var dobEditText: EditText
    private lateinit var saveChangesButton: Button
    private lateinit var deletePetButton: Button
    private lateinit var backButton: ImageButton

    private var selectedPhotoUri: Uri? = null
    private var savedPhotoPath: String? = null
    private var petId: Int = -1
    private var currentPet: Pet? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showImagePickerOptions()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            petAvatar.setImageURI(it)
            saveImageToInternalStorage(it)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            selectedPhotoUri?.let {
                petAvatar.setImageURI(it)
                saveImageToInternalStorage(it)
            }
        }
    }

    companion object {
        private const val ARG_PET_ID = "pet_id"

        fun newInstance(petId: Int): EditPetFragment {
            val fragment = EditPetFragment()
            val args = Bundle()
            args.putInt(ARG_PET_ID, petId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (_dbHelper == null) {
            _dbHelper = AppDatabaseHelper(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        petId = arguments?.getInt(ARG_PET_ID, -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_pet, container, false)

        initializeViews(view)
        setupSpinners()
        setupListeners()
        loadPetData()

        return view
    }

    private fun initializeViews(view: View) {
        petAvatar = view.findViewById(R.id.pet_avatar)
        editPhotoButton = view.findViewById(R.id.edit_photo_button)
        nameEditText = view.findViewById(R.id.name_edit_text)
        typeSpinner = view.findViewById(R.id.type_spinner)
        breedEditText = view.findViewById(R.id.breed_edit_text)
        sexSpinner = view.findViewById(R.id.sex_spinner)
        dobEditText = view.findViewById(R.id.dob_edit_text)
        saveChangesButton = view.findViewById(R.id.save_changes_button)
        deletePetButton = view.findViewById(R.id.delete_pet_button)
        backButton = view.findViewById(R.id.back_button)
    }

    private fun setupSpinners() {
        // Pet type spinner
        val petTypes = arrayOf("Dog", "Cat", "Bird", "Fish", "Rabbit", "Hamster", "Other")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, petTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        // Sex spinner
        val sexOptions = arrayOf("Male", "Female")
        val sexAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sexOptions)
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sexSpinner.adapter = sexAdapter
    }

    private fun setupListeners() {
        editPhotoButton.setOnClickListener {
            checkPermissionAndPickImage()
        }

        dobEditText.setOnClickListener {
            showDatePicker()
        }

        saveChangesButton.setOnClickListener {
            saveChanges()
        }

        deletePetButton.setOnClickListener {
            showDeleteConfirmation()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadPetData() {
        if (petId == -1) return

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM pet WHERE id = ?",
            arrayOf(petId.toString())
        )

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            val breed = cursor.getString(cursor.getColumnIndexOrThrow("breed"))
            val sex = cursor.getString(cursor.getColumnIndexOrThrow("sex"))
            val dob = cursor.getString(cursor.getColumnIndexOrThrow("dob"))
            val photoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"))

            // Populate fields
            nameEditText.setText(name)
            breedEditText.setText(breed)
            dobEditText.setText(dob)

            // Set spinners
            val typeAdapter = typeSpinner.adapter as ArrayAdapter<String>
            val typePosition = typeAdapter.getPosition(type)
            typeSpinner.setSelection(typePosition)

            val sexAdapter = sexSpinner.adapter as ArrayAdapter<String>
            val sexPosition = sexAdapter.getPosition(sex)
            sexSpinner.setSelection(sexPosition)

            // Load photo
            savedPhotoPath = photoPath
            if (photoPath != null && File(photoPath).exists()) {
                petAvatar.setImageURI(Uri.fromFile(File(photoPath)))
            } else {
                petAvatar.setImageResource(R.drawable.ic_pet_placeholder)
            }
        }
        cursor.close()
    }

    private fun checkPermissionAndPickImage() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                showImagePickerOptions()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> takePicture()
                1 -> pickImageLauncher.launch("image/*")
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun takePicture() {
        val photoFile = File(requireContext().getExternalFilesDir(null), "pet_photo_${System.currentTimeMillis()}.jpg")
        selectedPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(selectedPhotoUri)
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val filename = "pet_${petId}_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, filename)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            savedPhotoPath = file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Try to parse existing date if available
        val currentDate = dobEditText.text.toString()
        if (currentDate.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.parse(currentDate)
                date?.let { calendar.time = it }
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dobEditText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveChanges() {
        val name = nameEditText.text.toString().trim()
        val type = typeSpinner.selectedItem.toString()
        val breed = breedEditText.text.toString().trim()
        val sex = sexSpinner.selectedItem.toString()
        val dob = dobEditText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Please enter pet name", Toast.LENGTH_SHORT).show()
            return
        }

        if (dob.isEmpty()) {
            Toast.makeText(context, "Please select date of birth", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        try {
            val sql = """
                UPDATE pet 
                SET name = ?, type = ?, breed = ?, dob = ?, sex = ?, photo_path = ?
                WHERE id = ?
            """
            db.execSQL(sql, arrayOf(name, type, breed, dob, sex, savedPhotoPath, petId))

            Toast.makeText(context, "Pet updated successfully", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error updating pet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Pet")
        builder.setMessage("Are you sure you want to delete this pet? This action cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ ->
            deletePet()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deletePet() {
        val db = dbHelper.writableDatabase
        try {
            // Delete pet reminders first
            db.execSQL("DELETE FROM reminders WHERE pet_id = ?", arrayOf(petId.toString()))

            // Delete pet
            db.execSQL("DELETE FROM pet WHERE id = ?", arrayOf(petId.toString()))

            // Delete photo file if exists
            savedPhotoPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }

            Toast.makeText(context, "Pet deleted successfully", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error deleting pet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _dbHelper = null
    }
}