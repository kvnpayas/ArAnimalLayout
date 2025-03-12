package com.example.aranimallayout.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class DetectionOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 40f
        style = Paint.Style.FILL
    }

    var detections: List<Detection> = emptyList()
        set(value) {
            field = value
            invalidate() // Redraw the view when detections change
        }
    private val classLabels = listOf("cat", "chicken", "dog", "eagle", "monkey")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (detection in detections) {
            val location = detection.location
            val left = location[0] * width
            val top = location[1] * height
            val right = location[2] * width
            val bottom = location[3] * height
            val rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            canvas.drawRect(rect, boxPaint)
            val label = classLabels.getOrNull(detection.classId) ?: "Unknown"
            val text = "$label: ${String.format("%.2f", detection.score * 100)}%"
            canvas.drawText(text, left, top - 10, textPaint)
        }
    }
}