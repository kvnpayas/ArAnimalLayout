package com.example.aranimallayout.fragment
import com.example.aranimallayout.R

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.AnimalFunc
import com.example.aranimallayout.AnimalModel
import com.example.aranimallayout.databinding.ArsceneViewBinding
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode


class AnimalARView : Fragment() {

    private var _binding: ArsceneViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var animalRecyclerView: RecyclerView
    private var animalAdapter: AnimalFunc? = null
    private var animalList: List<AnimalModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ArsceneViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = binding.sceneView.apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }
        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT)

        animalList = ArrayList()
        (animalList as ArrayList<AnimalModel>).apply {
            add(AnimalModel("Cat", "models/cat.glb", "cat"))
            add(AnimalModel("Carabao", "models/cockatoo.glb", "carabao"))
            add(AnimalModel("Cockatoo", "models/cockatoo_parrot.glb", "cockatoo"))
            add(AnimalModel("Cow", "models/cow.glb", "cow"))
            add(AnimalModel("Dino", "models/brunette.glb", "dino"))
            add(AnimalModel("Dog", "models/dog.glb", "dog"))
            add(AnimalModel("Elephant", "models/waxwing.glb", "elephant"))
            add(AnimalModel("Monkey", "models/waxwing.glb", "monkey"))
            add(AnimalModel("Waxwing", "models/waxwing.glb", "waxwing"))
        }

        // Initialize RecyclerView
        animalRecyclerView = binding.animalRecyclerView
        animalRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        animalAdapter = AnimalFunc(requireContext(), animalList as ArrayList<AnimalModel>)
        animalRecyclerView.adapter = animalAdapter

        //mediaPlayer = MediaPlayer.create(requireContext(), R.raw.ad)

        sceneView.addChild(modelNode)

        animalAdapter!!.setOnItemClickListener { modelType ->
            loadModel(modelType)
        }
    }

    private fun loadModel(modelType: String) {
        val modelFile = when (modelType) {
            "Cat" -> "models/cat.glb"
            "Carabao" -> "models/waxwing.glb"
            "Cow" -> "models/cow.glb"
            "Dino" -> "models/brunette.glb"
            "Dog" -> "models/dog.glb"
            "Cockatoo" -> "models/cockatoo.glb"
            "Waxwing" -> "models/waxwing.glb"
            else -> {
                Log.w("MainActivity", "Unknown model type: $modelType")
                return
            }
        }
        modelNode.loadModelGlbAsync(
            glbFileLocation = modelFile,
            autoAnimate = true,
            scaleToUnits = 1f,
            onError = { exception ->
                Log.e("MainActivity", "Error loading model: $modelType - ${exception.message}")
            }
        ) {
            modelNode.anchor()
            sceneView.planeRenderer.isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}


