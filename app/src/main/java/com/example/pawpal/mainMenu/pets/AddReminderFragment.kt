package com.example.pawpal.mainMenu.pets

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.pawpal.AppDatabaseHelper
import com.example.pawpal.R
import java.text.SimpleDateFormat
import java.util.*

class AddReminderFragment : Fragment() {

    private var _dbHelper: AppDatabaseHelper? = null
    private val dbHelper: AppDatabaseHelper
        get() = _dbHelper ?: AppDatabaseHelper(requireContext()).also { _dbHelper = it }

    private lateinit var reminderTypeSpinner: Spinner
    private lateinit var frequencySpinner: Spinner
    private lateinit var feedPerDayEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var setReminderButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var feedPerDayLayout: LinearLayout

    private var petId: Int = -1
    private val selectedTimes = mutableListOf<String>()

    companion object {
        private const val ARG_PET_ID = "pet_id"

        fun newInstance(petId: Int): AddReminderFragment {
            val fragment = AddReminderFragment()
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
        val view = inflater.inflate(R.layout.fragment_add_reminder, container, false)

        initializeViews(view)
        setupSpinners()
        setupListeners()

        return view
    }

    private fun initializeViews(view: View) {
        reminderTypeSpinner = view.findViewById(R.id.reminder_type_spinner)
        frequencySpinner = view.findViewById(R.id.frequency_spinner)
        feedPerDayEditText = view.findViewById(R.id.feed_per_day_edit_text)
        timeEditText = view.findViewById(R.id.time_edit_text)
        setReminderButton = view.findViewById(R.id.set_reminder_button)
        backButton = view.findViewById(R.id.back_button)
        feedPerDayLayout = view.findViewById(R.id.feed_per_day_layout)
    }

    private fun setupSpinners() {
        // Reminder type spinner
        val reminderTypes = arrayOf("Feeding", "Medicine", "Grooming", "Exercise", "Vet Visit", "Other")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reminderTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderTypeSpinner.adapter = typeAdapter

        // Frequency spinner
        val frequencies = arrayOf("Daily", "Weekly", "Monthly", "Once")
        val frequencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frequencySpinner.adapter = frequencyAdapter

        // Show/hide feed per day based on reminder type
        reminderTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = reminderTypes[position]
                feedPerDayLayout.visibility = if (selectedType == "Feeding") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        setReminderButton.setOnClickListener {
            saveReminder()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val formattedTime = timeFormat.format(calendar.time)

                // Handle multiple times for feeding
                val reminderType = reminderTypeSpinner.selectedItem.toString()
                if (reminderType == "Feeding") {
                    val feedPerDay = feedPerDayEditText.text.toString().toIntOrNull() ?: 1

                    if (selectedTimes.size < feedPerDay) {
                        selectedTimes.add(formattedTime)
                        updateTimeDisplay()
                    } else {
                        Toast.makeText(context, "Maximum $feedPerDay times allowed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedTimes.clear()
                    selectedTimes.add(formattedTime)
                    updateTimeDisplay()
                }
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun updateTimeDisplay() {
        timeEditText.setText(selectedTimes.joinToString(", "))
    }

    private fun saveReminder() {
        val reminderType = reminderTypeSpinner.selectedItem.toString()
        val frequency = frequencySpinner.selectedItem.toString()
        val feedPerDay = if (reminderType == "Feeding") {
            feedPerDayEditText.text.toString().toIntOrNull() ?: 1
        } else {
            1
        }

        if (selectedTimes.isEmpty()) {
            Toast.makeText(context, "Please select at least one time", Toast.LENGTH_SHORT).show()
            return
        }

        if (reminderType == "Feeding" && selectedTimes.size != feedPerDay) {
            Toast.makeText(context, "Please select $feedPerDay times for feeding", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        try {
            for (time in selectedTimes) {
                val sql = """
                    INSERT INTO reminders (pet_id, reminder_type, frequency, time, feed_per_day, is_active) 
                    VALUES (?, ?, ?, ?, ?, 1)
                """
                db.execSQL(sql, arrayOf(petId, reminderType, frequency, time, feedPerDay))
            }

            Toast.makeText(context, "Reminder(s) set successfully", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error setting reminder", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _dbHelper = null
    }
}