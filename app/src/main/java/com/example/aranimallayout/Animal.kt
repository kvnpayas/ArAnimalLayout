package com.example.aranimallayout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Animal(
    val id: Int,
    val name: String,
    val briefDescription: String,
    val description: String,
    val imageUrl: String,
    val sound: String
) : Parcelable