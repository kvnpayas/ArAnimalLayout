package com.example.aranimallayout.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aranimallayout.databinding.FragmentScanAnimalBinding
import com.example.aranimallayout.util.AnimalClassifierHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanAnimalFragment : Fragment() {

    private var _binding: FragmentScanAnimalBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var animalClassifierHelper: AnimalClassifierHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanAnimalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the TFLite model
        try {
            animalClassifierHelper = AnimalClassifierHelper(requireContext(), "animal_classification.tflite", 2)
        } catch (e: Exception) {
            Log.e("ScanAnimalFragment", "Error initializing TFLite model", e)
            Toast.makeText(requireContext(), "Error initializing model", Toast.LENGTH_SHORT).show()
            return
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Image analysis
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val result = animalClassifierHelper.classifyImage(imageProxy)
                        Log.d("ScanAnimalFragment", "Raw FloatArray Output: ${result.joinToString()}")
                        // Find the index of the maximum value in the FloatArray
                        val bestClassIndex = result.indices.maxByOrNull { result[it] } ?: -1
                        val bestConfidence = result.maxOrNull() ?: 0f
                        val classLabels = listOf("Cat", "Dogs") // Replace with your actual class labels
                        val resultText = if (bestClassIndex != -1) {
                            "Result: ${classLabels[bestClassIndex]}\nConfidence: ${String.format("%.2f", bestConfidence * 100)}%"
                        } else {
                            "No results"
                        }
                        requireActivity().runOnUiThread {
                            binding.resultTextView.text = resultText
                        }
                        imageProxy.close()
                    }
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e("ScanAnimalFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
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
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        )
        { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}