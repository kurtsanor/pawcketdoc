package com.example.tracker.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.example.tracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class LayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val navBar = findViewById< BottomNavigationView>(R.id.bottomNavigationView)
        val backBtn = findViewById<ImageButton>(R.id.buttonBack)

        openFragment(HomeFragment())

        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { openFragment(HomeFragment()); true }
                R.id.nav_pets -> { openFragment(PetsFragment()); true }
                R.id.nav_account -> { openFragment(AccountFragment()); true }
                else -> false
            }
        }

        backBtn.setOnClickListener {
            supportFragmentManager.popBackStack()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            backBtn.visibility =
                if (supportFragmentManager.backStackEntryCount > 0) View.VISIBLE else View.GONE
        }

    }

        private fun openFragment (fragment: Fragment) {
            supportFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView2, fragment)
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}