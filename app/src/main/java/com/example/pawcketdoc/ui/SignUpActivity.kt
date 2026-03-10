package com.example.pawcketdoc.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.dto.SignUpRequest
import com.example.pawcketdoc.service.AuthService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var authService: AuthService

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }
        val signInLink = findViewById<TextView>(R.id.textViewSignIn)
        signInLink.setOnClickListener {
            val intent = Intent(this, MainActivity:: class.java)
            startActivity(intent)
            finish()
        }

        val buttonSignup = findViewById<Button>(R.id.buttonSignUp)
        val progressSignUp = findViewById<ProgressBar>(R.id.progressSignUp)

        val firstName = findViewById<TextInputEditText>(R.id.textViewFirstName)
        val surName = findViewById<TextInputEditText>(R.id.textViewLastName)
        val email = findViewById<TextInputEditText>(R.id.textViewEmail)
        val password = findViewById<TextInputEditText>(R.id.textViewPassword)
        val confirmPassword = findViewById<TextInputEditText>(R.id.textViewConfirmPassword)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonSignup.text = ""          // hide text
                buttonSignup.isEnabled = false  // prevent double click
                progressSignUp.visibility = View.VISIBLE
            } else {
                buttonSignup.text = "Sign Up"
                buttonSignup.isEnabled = true
                progressSignUp.visibility = View.GONE
            }
        }

        firstName.addTextChangedListener { if (it.toString().isBlank()) firstName.error = "Required field" }
        surName.addTextChangedListener { if (it.toString().isBlank()) surName.error = "Required field" }
        email.addTextChangedListener { if (!Patterns.EMAIL_ADDRESS.matcher(it.toString()).matches()) email.error = "Invalid email format" }
        password.addTextChangedListener { if (it.toString().isEmpty()) password.error = "Required field" }
        confirmPassword.addTextChangedListener { if (it.toString() != password.text.toString()) confirmPassword.error = "Passwords do not match" }

        fun areValidFields(): Boolean {
            return when {
                firstName.text.toString().isBlank() -> {
                    firstName.error = "Required field"
                    false
                }
                surName.text.toString().isBlank() -> {
                    surName.error = "Required field"
                    false
                }
                !Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches() -> {
                    email.error = "Invalid email format"
                    false
                }
                password.text.toString().isEmpty() -> {
                    password.error = "Required field"
                    false
                }
                password.text.toString().length < 6 -> {
                    password.error = "Must be at least 6 characters"
                    false
                }
                confirmPassword.text.toString() != password.text.toString() -> {
                    confirmPassword.error = "Passwords do not match"
                    false
                }
                else -> {
                    firstName.error = null
                    surName.error = null
                    email.error = null
                    password.error = null
                    confirmPassword.error = null
                    true
                }
            }
        }

        db = DatabaseProvider.getDatabase(this)
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        authService = AuthService(db.userDao(), db.credentialsDao(), firebaseAuth, firebaseFirestore)


        buttonSignup.setOnClickListener {
            if (!areValidFields()) return@setOnClickListener
            lifecycleScope.launch {
                try {
                    setLoading(true)
                    val signupRequest = SignUpRequest(
                        firstName.text.toString(),
                        surName.text.toString(),
                        email.text.toString(),
                        password.text.toString()
                    )
                    authService.register(signupRequest)
                    SnackbarUtil.showSuccess(
                        view = findViewById(android.R.id.content),
                        title = "Success",
                        message = "Account has been registered"
                    )
                    firstName.text?.clear()
                    surName.text?.clear()
                    email.text?.clear()
                    password.text?.clear()
                    confirmPassword.text?.clear()
                } catch (e: Exception) {
                    Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_LONG).show()
                }
                finally {
                    setLoading(false)
                }
            }
        }



    }
}