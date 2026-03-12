package com.example.pawcketdoc.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var authService: AuthService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private var enableValidation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val signInLink = findViewById<TextView>(R.id.textViewSignIn)
        val buttonSignup = findViewById<Button>(R.id.buttonSignUp)
        val progressSignUp = findViewById<ProgressBar>(R.id.progressSignUp)

        val firstName = findViewById<TextInputEditText>(R.id.textViewFirstName)
        val surName = findViewById<TextInputEditText>(R.id.textViewLastName)
        val email = findViewById<TextInputEditText>(R.id.textViewEmail)
        val password = findViewById<TextInputEditText>(R.id.textViewPassword)
        val confirmPassword = findViewById<TextInputEditText>(R.id.textViewConfirmPassword)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonSignup.text = ""
                buttonSignup.isEnabled = false
                progressSignUp.visibility = View.VISIBLE
            } else {
                buttonSignup.text = "Sign Up"
                buttonSignup.isEnabled = true
                progressSignUp.visibility = View.GONE
            }
        }

        fun clearForm() {
            enableValidation = false

            firstName.error = null
            surName.error = null
            email.error = null
            password.error = null
            confirmPassword.error = null

            firstName.text?.clear()
            surName.text?.clear()
            email.text?.clear()
            password.text?.clear()
            confirmPassword.text?.clear()

            firstName.post {
                enableValidation = true
            }
        }

        firstName.addTextChangedListener {
            if (!enableValidation) return@addTextChangedListener

            firstName.error = if (it.toString().isBlank()) "Required field" else null
        }

        surName.addTextChangedListener {
            if (!enableValidation) return@addTextChangedListener

            surName.error = if (it.toString().isBlank()) "Required field" else null
        }

        email.addTextChangedListener {
            if (!enableValidation) return@addTextChangedListener

            val value = it.toString()
            email.error = when {
                value.isBlank() -> "Required field"
                !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Invalid email format"
                else -> null
            }
        }

        password.addTextChangedListener {
            if (!enableValidation) return@addTextChangedListener

            val value = it.toString()
            password.error = when {
                value.isBlank() -> "Required field"
                value.length < 6 -> "Must be at least 6 characters"
                else -> null
            }

            val confirmValue = confirmPassword.text.toString()
            confirmPassword.error = when {
                confirmValue.isBlank() -> null
                confirmValue != value -> "Passwords do not match"
                else -> null
            }
        }

        confirmPassword.addTextChangedListener {
            if (!enableValidation) return@addTextChangedListener

            val value = it.toString()
            confirmPassword.error = when {
                value.isBlank() -> "Required field"
                value != password.text.toString() -> "Passwords do not match"
                else -> null
            }
        }

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

        signInLink.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                    clearForm()

                    delay(1500)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_LONG).show()
                } finally {
                    setLoading(false)
                }
            }
        }
    }
}