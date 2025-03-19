package com.example.aranimallayout.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.AnimalFunc
import com.example.aranimallayout.AnimalItem
import com.example.aranimallayout.Category
import com.example.aranimallayout.R
import com.example.aranimallayout.databinding.ArsceneViewBinding
import com.example.aranimallayout.util.JsonUtil
import com.example.aranimallayout.util.RecyclerItemClickListener
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import androidx.navigation.fragment.findNavController

class AnimalARView : Fragment() {

    private var _binding: ArsceneViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var animalRecyclerView: RecyclerView
    private var animalAdapter: AnimalFunc? = null
    private var categories: List<Category> = emptyList()
    private var currentAnimalItems: List<AnimalItem> = emptyList()

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

        categories = JsonUtil.getCategoriesFromAssets(requireContext())
        currentAnimalItems = categories.map { AnimalItem.CategoryItem(it) }

        // Initialize RecyclerView
        animalRecyclerView = binding.animalRecyclerView
        animalRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        animalAdapter = AnimalFunc(requireContext(), currentAnimalItems)
        animalRecyclerView.adapter = animalAdapter

        //mediaPlayer = MediaPlayer.create(requireContext(), R.raw.ad)

        sceneView.addChild(modelNode)

        animalAdapter!!.setOnItemClickListener { modelType ->
            loadModel(modelType)
        }
        animalAdapter!!.setOnBackClickListener {
            currentAnimalItems = categories.map { AnimalItem.CategoryItem(it) }
            animalAdapter?.updateData(currentAnimalItems)
        }

        animalAdapter!!.setOnItemClickListener { animalName ->
            loadModel(animalName)
        }
        animalRecyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(requireContext(), animalRecyclerView, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val clickedItem = currentAnimalItems[position]
                    when (clickedItem) {
                        is AnimalItem.CategoryItem -> {
                            val animals = clickedItem.category.animals
                            currentAnimalItems = mutableListOf<AnimalItem>(AnimalItem.BackItem).apply {
                                addAll(animals.map { AnimalItem.AnimalData(it) })
                            }
                            animalAdapter?.updateData(currentAnimalItems)
                        }
                        is AnimalItem.AnimalData -> {
                            val animalName = clickedItem.animal.name
                            loadModel(animalName)
                        }
                        is AnimalItem.BackItem -> {
                            currentAnimalItems = categories.map { AnimalItem.CategoryItem(it) }
                            animalAdapter?.updateData(currentAnimalItems)
                        }
                    }
                }

                override fun onLongItemClick(view: View?, position: Int) {
                    // Handle long item click if needed
                }
            })
        )
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
        //mediaPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //mediaPlayer.release()
    }
}