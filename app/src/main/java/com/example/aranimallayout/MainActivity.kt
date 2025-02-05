package com.example.aranimallayout

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottomAppBar)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floatingActionCamera)

        // Get the NavController from the NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.animalFragment || destination.id == R.id.animalArView) { // Replace with your AnimalFragment's ID
                bottomAppBar.visibility = View.GONE
                floatingActionButton.visibility = View.GONE
            } else {
                bottomAppBar.visibility = View.VISIBLE
                floatingActionButton.visibility = View.VISIBLE

            }
        }

        bottomNavigationView.setupWithNavController(navController)

        floatingActionButton.setOnClickListener {
            navController.navigate(R.id.action_global_animalArView)
        }


    }
}