package com.example.aranimallayout.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.internal.utils.ImageUtil.rotateBitmap
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aranimallayout.Animal
import com.example.aranimallayout.databinding.FragmentObjectDetectionBinding
import com.example.aranimallayout.network.RetrofitClient
import com.example.aranimallayout.network.models.Detection
import com.example.aranimallayout.network.models.DetectionResponse
import com.example.aranimallayout.util.JsonUtil
import com.google.android.filament.ToneMapper.Linear
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import androidx.navigation.fragment.findNavController
import com.example.aranimallayout.R

class ObjectDetectionFragment : Fragment() {

    private var _binding: FragmentObjectDetectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var resultTextView: TextView
    private lateinit var confidenceTextView: TextView
    private lateinit var capturedImageView: ImageView
    private var imageCapture: ImageCapture? = null
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var processingTextView: TextView
    private lateinit var captureAgainButton: Button
    private lateinit var captureButton: Button
    private lateinit var viewAnimalDetailsButton: Button
    private lateinit var loadingContainer: LinearLayout
    private lateinit var resultContainer: LinearLayout



    private val animalLabels = listOf("Cat", "Chicken", "Dog", "Eagle", "Monkey") // Your 5 animal labels

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentObjectDetectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultTextView = binding.resultTextView
        confidenceTextView = binding.confidenceTextView
        capturedImageView = binding.capturedImageView
        loadingProgressBar = binding.loadingProgressBar
        processingTextView = binding.processingTextView
        captureAgainButton = binding.captureAgainButton
        loadingContainer = binding.loadingStateContainer
        resultContainer = binding.resultStateContainer
        captureButton = binding.captureButton
        viewAnimalDetailsButton = binding.viewAnimalDetailsButton

        viewAnimalDetailsButton.setOnClickListener {
            val animalName = resultTextView.text.toString().removePrefix("Result: ").trim()
            navigateToAnimalDetails(animalName)
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        captureAgainButton.setOnClickListener {
            // Reset UI elements
            capturedImageView.visibility = View.GONE
            resultTextView.text = ""
            captureButton.visibility = View.VISIBLE // Show capture button
            resultContainer.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("ObjectDetectionFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        requireActivity().runOnUiThread {
            loadingContainer.visibility = View.VISIBLE
            processingTextView.visibility = View.VISIBLE
            captureButton.visibility = View.GONE
        }

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    processImage(bitmap)
                }

                override fun onError(exc: ImageCaptureException) {
                    requireActivity().runOnUiThread { // Ensure UI updates on main thread
                        loadingContainer.visibility = View.GONE
                        resultContainer.visibility = View.GONE
                        captureButton.visibility = View.VISIBLE
                        captureButton.isEnabled = true // Re-enable capture button
                    }
                    Log.e("ObjectDetectionFragment", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(
                        requireContext(),
                        "Photo capture failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun processImage(bitmap: Bitmap) {
        val apiKey = "YOUR_API_KEY" // Replace with your API key

        // Prepare the text parts
        val model =
            MultipartBody.Part.createFormData("model", "https://hub.ultralytics.com/models/C6mfCvPhbTsJLR07Nvx5")
        val imgsz = MultipartBody.Part.createFormData("imgsz", "640")
        val conf = MultipartBody.Part.createFormData("conf", "0.75")
        val iou = MultipartBody.Part.createFormData("iou", "0.45")

        // Prepare the file part
        val file = File(requireContext().cacheDir, "image.jpg")
        bitmapToFile(bitmap, file) // Convert Bitmap to File
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        // Make the Retrofit call
        val call = RetrofitClient.instance.detectObjects(apiKey, model, imgsz, conf, iou, imagePart)

        call.enqueue(object : Callback<DetectionResponse> {
            override fun onResponse(call: Call<DetectionResponse>, response: Response<DetectionResponse>) {
                if (response.isSuccessful) {
                    Log.d("ObjectDetectionFragment", "API Response: ${response.body()}")

                    val detectionResponse = response.body()
                    if (detectionResponse?.images?.isNotEmpty() == true) {
                        val detections = detectionResponse.images[0].results
                        Log.d("ObjectDetectionFragment", "Detections: $detections")

                        // Update UI on the main thread
                        requireActivity().runOnUiThread {
                            loadingContainer.visibility = View.GONE
                            captureButton.visibility = View.GONE // Hide capture button
                            resultContainer.visibility = View.VISIBLE

                            if (detections != null) {
                                Log.d("ObjectDetectionFragment", "Detections to draw: $detections")
                                val processedBitmap = drawDetections(bitmap, detections) // Draw boxes
                                val rotatedBitmap = rotateBitmap(processedBitmap) // Rotate here
                                capturedImageView.setImageBitmap(rotatedBitmap)
                                capturedImageView.visibility = View.VISIBLE
                                binding.previewView.visibility = View.GONE

                                if (detections.isNotEmpty()) {
                                    val detection = detections[0]
                                    val detectedName = detection.name?.lowercase() // Get the detected name and convert to lowercase

                                    if (detectedName != null) {
                                        val animalLabelsLower = animalLabels.map { it.lowercase() } // Convert animalLabels to lowercase

                                        if (detectedName in animalLabelsLower || detectedName.removeSuffix("s") in animalLabelsLower) {
                                            resultTextView.text =
                                                "Result: ${detection.name}"
                                            confidenceTextView.text = "Confidence: ${
                                                "%.2f".format(detection.confidence?.times(100))
                                            }%"
                                            viewAnimalDetailsButton.visibility = View.VISIBLE
                                        } else {
                                            resultTextView.text = "Result: Unknown class detected"
                                        }
                                    } else {
                                        resultTextView.text = "Result: Nothing detected"
                                    }
                                } else {
                                    resultTextView.text = "Result: Nothing detected"
                                }
                            } else {
                                Log.e("ObjectDetectionFragment", "Detections list is null after API call")
                                resultTextView.text = "API Error: Detections list is null"
                            }
                        }
                    } else {
                        Log.e("ObjectDetectionFragment", "Response body is null")
                        Toast.makeText(requireContext(), "API Error: Empty Response", Toast.LENGTH_SHORT)
                            .show()
                        resultTextView.text = "API Error: Empty Response"
                    }
                } else {
                    Log.e("ObjectDetectionFragment", "API Error: ${response.code()}")
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ObjectDetectionFragment", "Error Body: $errorBody")
                        Toast.makeText(requireContext(), "API Error", Toast.LENGTH_SHORT).show()
                        resultTextView.text = "API Error"
                    } catch (e: IOException) {
                        Log.e("ObjectDetectionFragment", "Error parsing error body: ${e.message}")
                        Toast.makeText(requireContext(), "API Error", Toast.LENGTH_SHORT).show()
                        resultTextView.text = "API Error"
                    }
                }
            }

            override fun onFailure(call: Call<DetectionResponse>, t: Throwable) {
                Log.e("ObjectDetectionFragment", "Network Error: ${t.message}")
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
                resultTextView.text = "Network Error"
            }
        })
    }

    private fun rotateBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f) // Rotate by 90 degrees (adjust if needed)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun bitmapToFile(bitmap: Bitmap, file: File) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()
        file.writeBytes(bitmapData)
    }

    private fun drawDetections(bitmap: Bitmap, detections: List<Detection>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        for (detection in detections) {
            val box = detection.box
            if (box != null) {
                val x1 = box.x1?.roundToInt()?.toFloat() ?: 0f
                val y1 = box.y1?.roundToInt()?.toFloat() ?: 0f
                val x2 = box.x2?.roundToInt()?.toFloat() ?: 0f
                val y2 = box.y2?.roundToInt()?.toFloat() ?: 0f

                val left = x1
                val top = y1
                val right = x2
                val bottom = y2

                Log.d(
                    "ObjectDetectionFragment",
                    "Box: x1=$x1, y1=$y1, x2=$x2, y2=$y2, Left=$left, Top=$top, Right=$right, Bottom=$bottom"
                )

                canvas.drawRect(left, top, right, bottom, paint)
            } else {
                Log.e("ObjectDetectionFragment", "Invalid bounding box coordinates: $box")
            }
        }
        return mutableBitmap
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val planeProxy = this.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun navigateToAnimalDetails(animalName: String) {
        val normalizedAnimalName = animalName.lowercase().removeSuffix("s")
        val animal = getAnimalByName(normalizedAnimalName)
        Log.d("ObjectDetectionFragment", "Animal Details: $animal")
        if (animal != null) {
            val bundle = Bundle().apply {
                putParcelable("animal", animal)
            }
            findNavController().navigate(R.id.action_objectDetectionFragment_to_animalDetailFragment, bundle)
        } else {
            Toast.makeText(requireContext(), "Animal details not found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAnimalByName(animalName: String): Animal? {
        val categories = JsonUtil.getCategoriesFromAssets(requireContext())
        for (category in categories) {
            val foundAnimal = category.animals.find { it.name.equals(animalName, ignoreCase = true) }
            if (foundAnimal != null) {
                return foundAnimal
            }
        }
        return null
    }
}