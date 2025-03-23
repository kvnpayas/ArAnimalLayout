package com.example.aranimallayout

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.media.MediaPlayer

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    var backgroundMusicMuted = false
    var backgroundMusicVolume = 1.0f // Initial volume

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottomAppBar)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floatingActionCamera)

        // Get the NavController from the NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.animalFragment ||
                destination.id == R.id.animalArView ||
                destination.id == R.id.cameraOptionFragment ||
                destination.id == R.id.objectDetectionFragment ||
                destination.id == R.id.animalDetailFragment
            ) {
                bottomAppBar.visibility = View.GONE
                floatingActionButton.visibility = View.GONE
            } else {
                bottomAppBar.visibility = View.VISIBLE
                floatingActionButton.visibility = View.VISIBLE

            }
        }

        bottomNavigationView.setupWithNavController(navController)

        floatingActionButton.setOnClickListener {
            navController.navigate(R.id.cameraOptionFragment)
        }

        startBackgroundMusic()
    }


    private fun startBackgroundMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.backgroundmusic) // Replace with your music file
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(backgroundMusicVolume, backgroundMusicVolume)
        mediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setBackgroundMusicMute(muted: Boolean) {
        backgroundMusicMuted = muted
        if (muted) {
            mediaPlayer?.setVolume(0f, 0f)
        } else {
            mediaPlayer?.setVolume(backgroundMusicVolume, backgroundMusicVolume)
        }
    }

    fun setMediaPlayerVolume(volume: Float){
        mediaPlayer?.setVolume(volume, volume)
    }

}

