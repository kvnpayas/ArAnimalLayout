package com.example.aranimallayout.network

data class Prediction(
    val class_name: String,
    val confidence: Float,
    val box: List<Float> // [x1, y1, x2, y2]
)