package com.example.tracker.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.tracker.R
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.dto.LoginRequest
import com.example.tracker.model.User
import com.example.tracker.service.AuthService
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var authService: AuthService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        db = DatabaseProvider.getDatabase(this)
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        authService = AuthService(db.userDao(), db.credentialsDao(), firebaseAuth, firebaseFirestore)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }

        val uId = firebaseAuth.currentUser?.uid

        if (!uId.isNullOrBlank()) {
            val homePage = Intent(this@MainActivity, LayoutActivity::class.java)
            startActivity(homePage)
            finish()
        }

        val buttonSignIn = findViewById<Button>(R.id.buttonSignIn)
        val email = findViewById<TextInputEditText>(R.id.emailField)
        val password = findViewById<TextInputEditText>(R.id.passwordField)
        val buttonGoogleLogin = findViewById<Button>(R.id.buttonGoogleLogin)

        email.addTextChangedListener {
            if (!Patterns.EMAIL_ADDRESS.matcher(it.toString()).matches()) {
                email.error = "Invalid email format!"
            }
        }

        // Password format preset.
        val passwordPattern = Regex(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
        )

        // Validates email if input is a valid email address.
        fun emailValidation(): Boolean {
            val eml = email.text.toString().trim()

            return when {
                eml.isBlank() -> {
                    email.error = "Required field"
                    false
                }
                !Patterns.EMAIL_ADDRESS.matcher(eml).matches() -> {
                    email.error = "Invalid email format!"
                    false
                }
                else -> {
                    email.error = null
                    true
                }
            }
        }

        // Validates whether password meets the password format.
        fun passwordValidation(): Boolean {
            val pwd = password.text.toString().trim()

            return when {
                pwd.isBlank() -> {
                    password.error = "Required field"
                    false
                }

                else -> {
                    password.error = null
                    true
                }
            }
        }


        val signUp = findViewById<TextView>(R.id.textViewSignUp)
        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity:: class.java)
            startActivity(intent)
        }

        buttonSignIn.setOnClickListener {
            if (!emailValidation() || !passwordValidation()) return@setOnClickListener
            lifecycleScope.launch {
                try {
                    val loginRequest = LoginRequest(
                        email.text.toString(),
                        password.text.toString()
                    )
                    authService.login(loginRequest)
                    Toast.makeText(this@MainActivity, "Login Successful", Toast.LENGTH_LONG).show()

                    val homePage = Intent(this@MainActivity, LayoutActivity::class.java)
                    finish()
                    startActivity(homePage)
                }  catch (e: FirebaseNetworkException) {
                    Toast.makeText(this@MainActivity, "No Internet Connection", Toast.LENGTH_LONG).show()

                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@MainActivity, "Incorrect Credentials", Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        buttonGoogleLogin.setOnClickListener {
            try {
                val currentUser = firebaseAuth.currentUser?.email
                Toast.makeText(this@MainActivity, currentUser.toString(), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_LONG).show()
            }
        }




    }

}