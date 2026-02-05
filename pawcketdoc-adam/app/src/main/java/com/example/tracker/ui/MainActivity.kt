package com.example.tracker.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tracker.R
import com.google.android.material.textfield.TextInputEditText


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }

        //Immutable variables
        val buttonSignIn = findViewById<Button>(R.id.buttonSignIn)
        val email = findViewById<TextInputEditText>(R.id.emailField)
        val password = findViewById<TextInputEditText>(R.id.passwordField)
        val buttonGoogleLogin = findViewById<Button>(R.id.buttonGoogleLogin)

        // Password format preset.
        val passwordPattern = Regex(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
        )

        // Validates email if input is a valid email address.
        fun emailValidation(): Boolean {
            val eml = email.text.toString().trim()

            return when {
                eml.isEmpty() -> {
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
                pwd.isEmpty() -> {
                    password.error = "Required field"
                    false
                }

                // Combined validation (length + rules)
                !(pwd.length >= 8 && passwordPattern.matches(pwd)) -> {
                    password.error = "Password must be at least 8 characters and contain at least:\n" +
                            "- 1 uppercase\n" +
                            "- 1 lowercase\n" +
                            "- 1 number\n" +
                            "- 1 special character"
                    false
                }

                else -> {
                    password.error = null
                    true
                }
            }
        }

        // Login after clicking enter on keyboard in password field.
        password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                buttonSignIn.performClick()
                true
            } else {
                false
            }
        }

        // Manual Login
        buttonSignIn.setOnClickListener {
            val emailOk = emailValidation()
            val passOk = passwordValidation()

            if (emailOk && passOk){
                Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show()

                val mainPage = Intent(this, LayoutActivity::class.java)
                startActivity(mainPage)
                finish() // ✅ closes LoginActivity so back won't return here
            } else {
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Login using Google OAuth
        buttonGoogleLogin.setOnClickListener {
            val mainPage = Intent(this, LayoutActivity::class.java)
            startActivity(mainPage)
            finish() // ✅ same here
        }
    }

}