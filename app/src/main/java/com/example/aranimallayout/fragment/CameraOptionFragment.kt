package com.example.aranimallayout.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.aranimallayout.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aranimallayout.databinding.FragmentCameraOptionBinding

class CameraOptionFragment : Fragment() {
    private var _binding: FragmentCameraOptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnScan.setOnClickListener {
            // Handle Scan button click (e.g., navigate to a scan fragment)
            // For now, let's just log a message
            findNavController().navigate(R.id.action_cameraOptionFragment_to_objectDetectionFragment)
        }

        binding.btnAr.setOnClickListener {
            // Navigate to AnimalARView when AR button is clicked
            findNavController().navigate(R.id.action_cameraOptionFragmentt_to_animalArView)
        }

        binding.backButtonCamera.setOnClickListener {
            findNavController().popBackStack() // Navigate back to the previous fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}