package com.example.aranimallayout.fragment

import android.animation.ObjectAnimator
import android.content.ContentValues
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.opengl.GLES30
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.AnimalFunc
import com.example.aranimallayout.AnimalItem
import com.example.aranimallayout.Category
import com.example.aranimallayout.databinding.ArsceneViewBinding
import com.example.aranimallayout.util.JsonUtil
import com.example.aranimallayout.util.RecyclerItemClickListener
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.OutputStream
import android.os.HandlerThread
import android.os.Looper
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.example.aranimallayout.Animal
import com.example.aranimallayout.R

import com.google.android.filament.Texture
import com.google.android.filament.Texture.InternalFormat
import com.google.android.filament.Texture.Sampler
import com.google.android.filament.Texture.Usage
import com.google.android.filament.Texture.PixelBufferDescriptor
import com.google.android.filament.TextureSampler
import com.google.ar.sceneform.math.Vector3
import java.nio.ByteBuffer

class AnimalARView : Fragment() {

    private var _binding: ArsceneViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode
    private lateinit var animalRecyclerView: RecyclerView
    private lateinit var captureButton: FloatingActionButton
    private var animalAdapter: AnimalFunc? = null
    private var categories: List<Category> = emptyList()
    private var currentAnimalItems: List<AnimalItem> = emptyList()
    private var currentAnimal: Animal? = null
    private var mediaPlayer: MediaPlayer? = null
    private var animalSoundVolume = 1.0f
    private var isPlaying = false
    private var currentSoundResourceId: Int? = null

    private var backgroundQuadNode: ArModelNode? = null
    private var isCameraBackground: Boolean = true
    private lateinit var backgroundNode: ArModelNode


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

        sceneView.onFrame = { frameTimeNanos ->
            Log.d("AnimalARView", "Frame rendered: $frameTimeNanos")

            GLES30.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        }

        binding.descriptionContainer.post {
            binding.descriptionContainer.translationY =
                -binding.descriptionContainer.height.toFloat()
            binding.descriptionContainer.visibility = View.GONE
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


        captureButton = binding.captureCamera
        binding.leftButtonLayout.visibility = View.GONE

        backgroundNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT)
        sceneView.addChild(backgroundNode)

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
            RecyclerItemClickListener(
                requireContext(),
                animalRecyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val clickedItem = currentAnimalItems[position]
                        when (clickedItem) {
                            is AnimalItem.CategoryItem -> {
                                val animals = clickedItem.category.animals
                                currentAnimalItems =
                                    mutableListOf<AnimalItem>(AnimalItem.BackItem).apply {
                                        addAll(animals.map { AnimalItem.AnimalData(it) })
                                    }
                                animalAdapter?.updateData(currentAnimalItems)
                                binding.leftButtonLayout.visibility = View.GONE
                            }

                            is AnimalItem.AnimalData -> {
                                val animalName = clickedItem.animal.name
                                loadModel(animalName)
                            }

                            is AnimalItem.BackItem -> {
                                currentAnimalItems = categories.map { AnimalItem.CategoryItem(it) }
                                animalAdapter?.updateData(currentAnimalItems)
                                binding.leftButtonLayout.visibility = View.GONE
                            }
                        }
                    }

                    override fun onLongItemClick(view: View?, position: Int) {

                    }
                })
        )

        captureButton.setOnClickListener {
            captureScreenshot()
        }

    }

    private fun loadModel(modelType: String) {
        val animal: Animal? = categories.flatMap { it.animals }.find { it.name == modelType }
        currentAnimal = animal
        mediaPlayer?.stop()
        resetPlayButton()
        hideDescription()
        if (animal != null) {

            if (animal.sound.isNullOrEmpty()) {
                binding.modelSound.visibility = View.GONE
            } else {
                binding.modelSound.visibility = View.VISIBLE
            }

            val modelFile = "models/${animal.model}"
            modelNode.loadModelGlbAsync(
                glbFileLocation = modelFile,
                autoAnimate = true,
                scaleToUnits = 1f,
                onError = { exception ->
                    Log.e("AnimalARView", "Error loading model: $modelType - ${exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error loading model: $modelType",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.leftButtonLayout.visibility = View.GONE
                }
            ) {
                modelNode.anchor()
                sceneView.planeRenderer.isVisible = false
                binding.leftButtonLayout.visibility = View.VISIBLE

                binding.modelInfo.setOnClickListener {
                    showDescription(animal) // Pass the Animal object
                }

                binding.modelSound.setOnClickListener {
                    Log.e("AnimalARViewPlay", "modelSound clicked")
                    currentAnimal?.let { animal ->
                        if (isPlaying) {
                            pauseSound()
                        } else {
                            playSound(animal.sound)
                        }
                    } ?: run {
                        Log.d("AnimalARViewPlay", "currentAnimal is null")
                    }
                }

                binding.modelBackground.setOnClickListener {
                    Log.d("AnimalARViewBg", "modelBackground clicked")
                    if (currentAnimal != null) {
                        Log.d("AnimalARViewBg", "currentAnimal is not null")
                        toggleBackground(currentAnimal!!)
                    } else {
                        Log.d("AnimalARViewBg", "currentAnimal is null")
                    }
                }

                binding.actionButtonsLayout.removeAllViews() // Clear any existing buttons
                animal.animations.forEach { animationName ->
                    val animationButton = FloatingActionButton(requireContext())
                    animationButton.apply {
                        id = View.generateViewId() // Generate a unique ID for the button
                        size = FloatingActionButton.SIZE_MINI
                        setImageResource(R.drawable.bg_icon) // You can use a different icon
                        ImageViewCompat.setImageTintList(
                            this,
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.accent
                                )
                            )
                        )
                        backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        scaleX = 0.5f
                        scaleY = 0.5f
                        setOnClickListener {
                            val actionModel = "models/${animal.name}-$animationName.glb".lowercase()
                            Log.d("AnimationLoad", "Attempting to load: $actionModel")
                            modelNode.loadModelGlbAsync(
                                glbFileLocation = actionModel,
                                autoAnimate = true,
                                scaleToUnits = 1f,
                                onError = { exception ->
                                    Log.e("AnimationLoadError", "Error loading $actionModel: ${exception.message}")
                                    Toast.makeText(requireContext(), "Error loading animation: $animationName", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                modelNode.anchor()
                            }
                        }
                        Log.d("AnimalARViewAnimation", "animation: $animationName")
                        val layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.setMargins(0, 8.dpToPx(), 0, 0) // Add some margin
                        this.layoutParams = layoutParams
                    }
                    binding.actionButtonsLayout.addView(animationButton)
                }
            }
        } else {
            Log.w("AnimalARView", "Model not found: $modelType")
            Toast.makeText(requireContext(), "Model not found: $modelType", Toast.LENGTH_SHORT)
                .show()
            binding.leftButtonLayout.visibility = View.GONE
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun captureScreenshot() {
        Log.d("AnimalARView", "captureScreenshot called")
        captureButton.visibility = View.GONE

        val sceneView = binding.sceneView
        val bitmap = Bitmap.createBitmap(sceneView.width, sceneView.height, Bitmap.Config.ARGB_8888)

        val visibleRect = Rect()
        sceneView.getLocalVisibleRect(visibleRect)

        PixelCopy.request(
            sceneView,
            visibleRect,
            bitmap,
            { result ->
                Handler(Looper.getMainLooper()).post {
                    when (result) {
                        PixelCopy.SUCCESS -> {
                            saveBitmapToGallery(bitmap)
                            captureButton.visibility = View.VISIBLE
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                "Screenshot failure: $result",
                                Toast.LENGTH_SHORT
                            ).show()
                            captureButton.visibility = View.VISIBLE
                        }
                    }
                }
            },
            Handler(HandlerThread("screenshot").apply { start() }.looper)
        )
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "AR_Animal_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = requireContext().contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
//                Toast.makeText(requireContext(), "Screenshot saved to Gallery", Toast.LENGTH_SHORT).show()
                Toast.makeText(
                    requireContext(),
                    "Image captured and saved to Gallery!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                Log.e("AnimalARView", "Error saving image to gallery: ${e.message}")
                Toast.makeText(requireContext(), "Failed to save screenshot.", Toast.LENGTH_SHORT)
                    .show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Failed to save screenshot.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun playSound(soundFileName: String) {
        try {
            val soundResourceId =
                resources.getIdentifier(soundFileName, "raw", requireContext().packageName)
            if (soundResourceId != 0) {

                if (soundResourceId != currentSoundResourceId) {

                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer().apply {
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                        setAudioAttributes(audioAttributes)
                        setDataSource(
                            requireContext(),
                            android.net.Uri.parse("android.resource://${requireContext().packageName}/$soundResourceId")
                        )
                        prepare()
                        setVolume(animalSoundVolume, animalSoundVolume)
                        setOnCompletionListener {
                            resetPlayButton()
                        }
                    }
                    currentSoundResourceId = soundResourceId
                }

                mediaPlayer?.start()
                isPlaying = true
                binding.modelSound.setImageResource(R.drawable.pause_sound)
            } else {
                Log.e("AnimalARView", "Sound resource not found: $soundFileName")
                Toast.makeText(requireContext(), "Sound not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AnimalARView", "Error playing sound: ${e.message}")
            Toast.makeText(requireContext(), "Error playing sound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseSound() {
        mediaPlayer?.pause()
        isPlaying = false
        binding.modelSound.setImageResource(R.drawable.play_sound)
    }

    private fun resetPlayButton() {
        isPlaying = false
        binding.modelSound.setImageResource(R.drawable.play_sound)
    }

    private fun showDescription(animal: Animal) {
        if (binding.descriptionContainer.visibility == View.GONE) {

            binding.descriptionContainer.visibility = View.VISIBLE
            binding.modelTitle.text = animal.name
            binding.modelDescription.text = animal.description
            binding.modelFunFact.text = animal.funFact
            binding.modelSoundDesc.text = animal.soundDesc
            binding.modelLifeSpan.text = animal.lifeSpan

            val animator = ObjectAnimator.ofFloat(
                binding.descriptionContainer,
                "translationY",
                -binding.descriptionContainer.height.toFloat(),
                0f
            )
            animator.duration = 500
            animator.start()
        } else {
            // Hide the description
            hideDescription()
        }
    }

    private fun hideDescription() {
        val animator = ObjectAnimator.ofFloat(
            binding.descriptionContainer,
            "translationY",
            -binding.descriptionContainer.height.toFloat()
        )
        animator.duration = 500
        animator.start()

        animator.doOnEnd { // Use doOnEnd to ensure visibility is changed after animation
            binding.descriptionContainer.visibility = View.GONE
        }
    }

    private fun toggleBackground(animal: Animal) {
        Log.d("AnimalARViewBg", "toggleBackground called")
        Log.d("AnimalARViewBg", "isCameraBackground: $isCameraBackground")
        if (isCameraBackground) {
            Log.d("AnimalARViewBg", "isCameraBackground is true, loading background")
            loadBackgroundImage(animal)
            isCameraBackground = false
        } else {
            Log.d("AnimalARViewBg", "isCameraBackground is false, removing background")
            removeBackgroundImage()
            isCameraBackground = true
        }
    }

    private fun loadBackgroundImage(animal: Animal) {
        val modelBg = animal.backgroundImage


        backgroundNode.apply {
            val modelFile = "models/$modelBg"
            Log.d("AnimalARViewBG", "modelFile: $modelFile")
            loadModelGlbAsync(
                glbFileLocation = modelFile,
                autoAnimate = false,
                scaleToUnits = 2f,
                onError = { exception ->
                    Log.e("AnimalARViewBg", "Error loading background.glb: ${exception.message}")
                    exception.printStackTrace()
                }
            ) {
                anchor()
//                position = io.github.sceneview.math.Position(5f, 2f, -10f)
                rotation = io.github.sceneview.math.Rotation(0f, 90f, 0f)
                sceneView.planeRenderer.isVisible = false

                Log.d("AnimalARViewBG", "backgroundQuadNode added to sceneView")

            }
        }
        Log.d("AnimalARViewBG", "End")
    }

    private fun removeBackgroundImage() {
        backgroundNode?.let {
            sceneView.removeChild(it)
            it.destroy()
            backgroundNode =
                ArModelNode(sceneView.engine, PlacementMode.INSTANT) // create a new node.
            sceneView.addChild(backgroundNode)
        }
    }


    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}