package com.example.aranimallayout.fragment

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load
import com.example.aranimallayout.Animal
import com.example.aranimallayout.R
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController

class AnimalDetailFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var playButton: ImageButton? = null
    private var playButtonDesc: TextView? = null
    private var playButtonLayout: LinearLayout? = null
    private var soundResId: Int? = null
    private var animalSoundVolume = 1.0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_animal_detail, container, false)

        val animal = arguments?.getParcelable<Animal>("animal")

        if (animal != null) {
            val animalImage = view.findViewById<ImageView>(R.id.animalDetailImage)
            val animalName = view.findViewById<TextView>(R.id.animalDetailName)
            val animalDescription = view.findViewById<TextView>(R.id.animalDetailDescription)
            val soundDescription = view.findViewById<TextView>(R.id.soundDescription)
            val animalScientificName = view.findViewById<TextView>(R.id.animalScientificName)
            val animalTagalogName = view.findViewById<TextView>(R.id.animalTagalogName)
            val animalFunFact = view.findViewById<TextView>(R.id.animalFunFact)
            val animalLifeSpan = view.findViewById<TextView>(R.id.animalLifeSpan)
            val backButton = view.findViewById<AppCompatButton>(R.id.backButtonDetail)
            val animalNameTextView = view.findViewById<TextView>(R.id.animalNameTextView)
//            val playButton = view.findViewById<ImageButton>(R.id.playButton)
            playButton = view.findViewById(R.id.playButton)
            playButtonDesc = view.findViewById(R.id.playButtonDesc)
            playButtonLayout = view.findViewById(R.id.playButtonLayout)

            val imageResId = resources.getIdentifier(
                animal.imageUrl,
                "drawable",
                requireContext().packageName
            )
            animalImage.load(imageResId)
            animalName.text = animal.name
            animalDescription.text = animal.description
            animalNameTextView.text = animal.name
            animalScientificName.text = animal.scientificName
            animalTagalogName.text = animal.tagalogName
            animalFunFact.text = animal.funFact
            animalLifeSpan.text = animal.lifeSpan
            soundDescription.text = animal.soundDesc

            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            soundResId = animal.sound?.let {
                resources.getIdentifier(it, "raw", requireContext().packageName)
            }

            if (soundResId != 0 && soundResId != null) {
                mediaPlayer = MediaPlayer().apply {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM) // Use alarm usage
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    setAudioAttributes(audioAttributes)
                    setDataSource(requireContext(), android.net.Uri.parse("android.resource://${requireContext().packageName}/$soundResId"))
                    prepare()
                    setVolume(animalSoundVolume, animalSoundVolume) // set volume
                    setOnCompletionListener {
                        resetPlayButton()
                    }
                }

                playButton?.setOnClickListener {
                    if (isPlaying) {
                        pauseSound()
                    } else {
                        playSound()
                    }
                }
            } else {
                playButtonLayout?.visibility = View.GONE
            }
        }

        return view
    }

    private fun playSound() {
        mediaPlayer?.start()
        isPlaying = true
        playButton?.setImageResource(R.drawable.pause_sound) // Change to pause icon
    }

    private fun pauseSound() {
        mediaPlayer?.pause()
        isPlaying = false
        playButton?.setImageResource(R.drawable.play_sound) // Change to play icon
    }

    private fun resetPlayButton() {
        isPlaying = false
        playButton?.setImageResource(R.drawable.play_sound) // Change to play icon
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}