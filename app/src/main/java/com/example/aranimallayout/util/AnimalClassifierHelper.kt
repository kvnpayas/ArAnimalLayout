package com.example.aranimallayout.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.fragment.app.add
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.NormalizeOp // Correct import
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.MappedByteBuffer

class AnimalClassifierHelper(
    private val context: Context,
    private val modelFileName: String,
    private val numThreads: Int
) {

    private var interpreter: Interpreter? = null
    private var inputImageWidth: Int = 448
    private var inputImageHeight: Int = 448
    private lateinit var imageProcessor: ImageProcessor

    init {
        try {
            interpreter = Interpreter(loadModelFile(modelFileName), Interpreter.Options().setNumThreads(numThreads))
            imageProcessor = ImageProcessor.Builder()
                .add(NormalizeOp(0.0f, 255.0f))
                .build()
        } catch (e: IOException) {
            Log.e("AnimalClassifierHelper", "Error initializing TFLite model", e)
        }
    }

    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        return FileUtil.loadMappedFile(context, modelFileName)
    }

    fun classifyImage(imageProxy: ImageProxy): FloatArray {
        val bitmap = imageProxy.toBitmap()
        val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, inputImageWidth, inputImageHeight, true)
        var tensorImage = TensorImage.fromBitmap(resizedBitmap)
        tensorImage = imageProcessor.process(tensorImage)
        val outputTensorBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 2), org.tensorflow.lite.DataType.FLOAT32)
        interpreter?.run(tensorImage.buffer, outputTensorBuffer.buffer)
        return outputTensorBuffer.floatArray
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val planeProxy = this.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}