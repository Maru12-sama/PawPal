package com.example.pawpal.mainMenu.guides

data class GuideItem(
    val title: String,
    val source: String,     // "YouTube", "Blog", etc.
    val date: String,
    val imageResId: Int     // e.g. R.drawable.img_guide1
)
