package com.example.pawcketdoc.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pawcketdoc.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class LayoutActivity : AppCompatActivity() {

    private var bottomInset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_layout)

        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val backBtn = findViewById<ImageButton>(R.id.buttonBack)
        val navHostView = findViewById<View>(R.id.nav_host_fragment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomInset = systemBars.bottom
            v.setPadding(0, systemBars.top, 0, 0)
            navBar.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navBar.setupWithNavController(navController)

        backBtn.setOnClickListener {
            if (!navController.popBackStack()) {
                finish()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            backBtn.visibility = when (destination.id) {
                R.id.homeFragment, R.id.petsFragment, R.id.accountFragment -> View.GONE
                else -> View.VISIBLE
            }

            when (destination.id) {
                R.id.homeFragment, R.id.petsFragment, R.id.accountFragment -> {
                    navBar.visibility = View.VISIBLE
                    navHostView.setPadding(0, 0, 0, 0)
                }
                else -> {
                    navBar.visibility = View.GONE
                    navHostView.setPadding(0, 0, 0, bottomInset)
                }
            }
        }
    }
}