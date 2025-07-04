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

class AddPetFragment : Fragment() {

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
    private lateinit var savePetButton: Button
    private lateinit var backButton: ImageButton

    private var selectedPhotoUri: Uri? = null
    private var savedPhotoPath: String? = null
    private var ownerId: Int? = null

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
        val view = inflater.inflate(R.layout.fragment_add_pet, container, false)

        initializeViews(view)
        setupSpinners()
        setupListeners()

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
        savePetButton = view.findViewById(R.id.save_pet_button)
        backButton = view.findViewById(R.id.back_button)

        // Set default placeholder image
        petAvatar.setImageResource(R.drawable.ic_pet_placeholder)
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

        savePetButton.setOnClickListener {
            savePet()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
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
            val filename = "pet_${System.currentTimeMillis()}.jpg"
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

    private fun savePet() {
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

        if (ownerId == null || ownerId!! <= 0) {
            Toast.makeText(context, "Invalid owner ID", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        try {
            val sql = """
                INSERT INTO pet (name, type, breed, dob, sex, owner_id, photo_path) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """
            db.execSQL(sql, arrayOf(name, type, breed, dob, sex, ownerId, savedPhotoPath))

            Toast.makeText(context, "Pet added successfully", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving pet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _dbHelper = null
    }
}