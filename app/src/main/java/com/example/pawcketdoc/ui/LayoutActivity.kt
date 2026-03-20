package com.example.pawcketdoc.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pawcketdoc.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class LayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                .setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val navBar = findViewById< BottomNavigationView>(R.id.bottomNavigationView)
        val backBtn = findViewById<ImageButton>(R.id.buttonBack)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navBar.setupWithNavController(navController)

        backBtn.setOnClickListener {
            navController.popBackStack()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // show back button only when not on bottom nav destinations
            backBtn.visibility = when (destination.id) {
                R.id.homeFragment, R.id.petsFragment, R.id.accountFragment -> View.GONE
                else -> View.VISIBLE
            }
        }

    }
}