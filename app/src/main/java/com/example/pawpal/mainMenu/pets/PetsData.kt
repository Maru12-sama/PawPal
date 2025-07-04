package com.example.pawpal.mainMenu.pets

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pet(
    val id: Int,
    val name: String,
    val type: String, // Cat, Dog, etc.
    val age: Int,
    val breed: String,
    val sex: String,
    val photoPath: String? = null
) : Parcelable