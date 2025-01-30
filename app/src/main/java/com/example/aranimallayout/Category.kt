package com.example.aranimallayout 

data class Category(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val animals: List<Animal>
)