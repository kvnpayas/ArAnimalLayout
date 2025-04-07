package com.example.aranimallayout.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class AnimalClassifierHelper(
    private val context: Context,
    private val modelFileName: String,
    private val numClasses: Int = 5
) {

    private var tflite: Interpreter? = null
    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var modelInputSize: Int = 0

    init {
        try {
            Log.d("AnimalClassifierHelper", "Loading model: $modelFileName")
            val tfliteModel = loadModelFile(modelFileName)
            tflite = Interpreter(tfliteModel)
            val inputShape = tflite?.getInputTensor(0)?.shape()
            Log.d("AnimalClassifierHelper", "Input Shape: ${inputShape?.contentToString()}")
            inputImageWidth = inputShape?.get(1) ?: 0
            inputImageHeight = inputShape?.get(2) ?: 0
            modelInputSize = inputImageWidth * inputImageHeight * 3 * 4
            Log.d("AnimalClassifierHelper", "Model loaded successfully")
            val outputTensor = tflite?.getOutputTensor(0) // Assuming the output is the first tensor
            val outputShape = outputTensor?.shape()
            Log.d("AnimalClassifierHelper", "Output Shape: ${outputShape?.contentToString()}")
        } catch (e: IOException) {
            Log.e("AnimalClassifierHelper", "Error initializing TFLite model", e)
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classifyImage(bitmap: Bitmap): List<FloatArray> { // Change return type
        if (tflite == null) {
            Log.e("AnimalClassifierHelper", "TFLite model not initialized")
            return emptyList()
        }
        Log.d("AnimalClassifierHelper", "Classifying image")
        val resizedBitmap =
            Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)

        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)
        Log.d("AnimalClassifierHelper", "Bitmap converted to ByteBuffer")
        val output = Array(1) { Array(9) { FloatArray(8400) } }
        tflite?.run(byteBuffer, output) // Run inference
        Log.d("AnimalClassifierHelper", "Inference completed")

        val flattenedOutput = FloatArray(9 * 8400) // Create a FloatArray of the correct size
        for (i in 0 until 9) {
            for (j in 0 until 8400) {
                flattenedOutput[i * 8400 + j] = output[0][i][j]
            }
        }
        Log.d("AnimalClassifierHelper", "Size of flattened output: ${flattenedOutput.size}")
        return listOf(flattenedOutput)
    }

    data class DetectionResult(val x: Float, val y: Float, val width: Float, val height: Float, val confidence: Float, val classId: Int)

    fun FloatArray.indexOfMax(): Int {
        return this.indices.maxByOrNull { this[it] } ?: -1
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until inputImageWidth) {
            for (j in 0 until inputImageHeight) {
                val input = intValues[pixel++]

                byteBuffer.putFloat(((input shr 16 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((input shr 8 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((input and 0xFF) / 255.0f))
            }
        }
        return byteBuffer
    }
}