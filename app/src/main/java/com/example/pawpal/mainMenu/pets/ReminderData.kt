package com.example.pawpal.mainMenu.pets

data class ReminderData(
    val id: Int = 0,
    val petId: Int,
    val title: String? = null,
    val description: String? = null,
    val reminderType: String,
    val frequency: String,
    val date: String? = null,
    val time: String,
    val feedPerDay: Int = 1,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: String? = null
)
