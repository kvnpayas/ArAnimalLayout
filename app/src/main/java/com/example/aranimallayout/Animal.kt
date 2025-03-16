package com.example.aranimallayout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Animal(
    val id: Int,
    val name: String,
    val tagalogName: String,
    val scientificName: String,
    val lifeSpan: String,
    val funFact: String,
    val description: String,
    val imageUrl: String,
    val sound: String,
    val soundDesc: String
) : Parcelable