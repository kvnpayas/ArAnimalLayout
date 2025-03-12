package com.example.aranimallayout.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.IOException
import java.nio.MappedByteBuffer
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp

class ObjectDetectorHelper(
    private val context: Context,
    private val modelFileName: String,
    private val numThreads: Int
) {

    private var interpreter: Interpreter? = null
    private var inputImageWidth: Int = 640
    private var inputImageHeight: Int = 640
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var outputBuffer: Array<Array<FloatArray>>
    private val numClasses = 5
    private val numDetections = 8400

    init {
        try {
            interpreter =
                Interpreter(
                    loadModelFile(modelFileName),
                    Interpreter.Options().setNumThreads(numThreads)
                )
            imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f))
                .build()
            val outputTensorShape = interpreter?.getOutputTensor(0)?.shape()
            if (outputTensorShape != null && outputTensorShape.isNotEmpty()) {
                outputBuffer =
                    Array(outputTensorShape[0]) {
                        Array(outputTensorShape[1]) { FloatArray(outputTensorShape[2]) }
                    }
            } else {
                Log.e("ObjectDetectorHelper", "Invalid output tensor shape")
            }
        } catch (e: IOException) {
            Log.e("ObjectDetectorHelper", "Error initializing TFLite model", e)
        }
    }

    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        return FileUtil.loadMappedFile(context, modelFileName)
    }

    fun detectObjects(imageProxy: ImageProxy): DetectionResult {
        val bitmap = imageProxy.toBitmap()
        val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(rotatedBitmap)
        tensorImage = imageProcessor.process(tensorImage)
        val byteBuffer = tensorImage.buffer
        val inputs = arrayOf<Any>(byteBuffer)
        val outputs = mapOf(0 to outputBuffer)
        interpreter?.runForMultipleInputsOutputs(inputs, outputs)

        val detections = processOutput(outputBuffer)
        Log.d("ObjectDetection", "Raw output tensor: ${outputBuffer.contentDeepToString()}")
        return DetectionResult(detections, rotatedBitmap)
    }

    fun detectObjects(bitmap: Bitmap): DetectionResult {
        val rotatedBitmap = bitmap // No need to rotate if you're taking a single picture
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(rotatedBitmap)
        tensorImage = imageProcessor.process(tensorImage)
        val byteBuffer = tensorImage.buffer
        val inputs = arrayOf<Any>(byteBuffer)
        val outputs = mapOf(0 to outputBuffer)
        interpreter?.runForMultipleInputsOutputs(inputs, outputs)

        val detections = processOutput(outputBuffer)
        Log.d("ObjectDetection", "Raw output tensor: ${outputBuffer.contentDeepToString()}")
        return DetectionResult(detections, rotatedBitmap)
    }

    private fun processOutput(output: Array<Array<FloatArray>>): List<Detection> {
        val detections = mutableListOf<Detection>()

        for (i in 0 until numDetections) {
            val detectionData = output[0][0]

            val classScoresStartIndex = 4 + i * numClasses
            val classScoresEndIndex = 4 + (i + 1) * numClasses
            if (classScoresEndIndex <= detectionData.size) {
                val classScores =
                    detectionData.copyOfRange(classScoresStartIndex, classScoresEndIndex)
                val maxEntry = classScores.withIndex().maxByOrNull { it.value }

                if (maxEntry != null && maxEntry.value > 0.5f) {
                    val classId = maxEntry.index

                    val locationStartIndex = i * 4
                    val locationEndIndex = (i + 1) * 4
                    if (locationEndIndex <= detectionData.size) {
                        val location =
                            detectionData.copyOfRange(locationStartIndex, locationEndIndex)
                        // Assuming location data is [x1, y1, x2, y2]
                        detections.add(Detection(classId, maxEntry.value, location))
                    }
                }
            }
        }
        return applyNMS(detections, 0.5f)
    }

    private fun applyNMS(detections: List<Detection>, iouThreshold: Float): List<Detection> {
        val sortedDetections = detections.sortedByDescending { it.score }.toMutableList()
        val finalDetections = mutableListOf<Detection>()

        while (sortedDetections.isNotEmpty()) {
            val currentDetection = sortedDetections.removeAt(0)
            finalDetections.add(currentDetection)

            val iterator = sortedDetections.iterator()
            while (iterator.hasNext()) {
                val otherDetection = iterator.next()
                if (iou(currentDetection, otherDetection) > 0.4f) { // Lowered IoU threshold
                    iterator.remove()
                }
            }
        }
        return finalDetections
    }

    private fun iou(detection1: Detection, detection2: Detection): Float {
        val box1 = detection1.location
        val box2 = detection2.location

        val x1 = maxOf(box1[0], box2[0])
        val y1 = maxOf(box1[1], box2[1])
        val x2 = minOf(box1[2], box2[2])
        val y2 = minOf(box1[3], box2[3])

        val intersectionArea = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val box1Area = (box1[2] - box1[0]) * (box1[3] - box1[1])
        val box2Area = (box2[2] - box2[0]) * (box2[3] - box2[1])

        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    internal fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
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

data class Detection(val classId: Int, val score: Float, val location: FloatArray)
data class DetectionResult(val detections: List<Detection>, val bitmap: Bitmap)