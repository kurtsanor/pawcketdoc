package com.example.pawcketdoc.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.dto.LoginRequest
import com.example.pawcketdoc.service.AuthService
import com.example.pawcketdoc.service.SyncService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var authService: AuthService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var syncService: SyncService
    private lateinit var googleSignInClient: GoogleSignInClient

    @RequiresApi(Build.VERSION_CODES.O)
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            SnackbarUtil.showError(
                view = findViewById(android.R.id.content),
                title = "Error",
                message = "Google sign-in failed: ${e.message}"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        db = DatabaseProvider.getDatabase(this)
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        authService = AuthService(db.userDao(), db.credentialsDao(), firebaseAuth, firebaseFirestore)
        syncService = SyncService(
            firebaseFirestore,
            db.userDao(),
            db.petDao(),
            db.appointmentDao(),
            db.medicationDao(),
            db.growthDao(),
            db.vaccinationDao(),
            db.medicalRecordDao(),
            db.documentDao()
        )

        setupGoogleSignIn()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }

        val buttonSignIn = findViewById<Button>(R.id.buttonSignIn)
        val email = findViewById<TextInputEditText>(R.id.emailField)
        val password = findViewById<TextInputEditText>(R.id.passwordField)
        val buttonGoogleLogin = findViewById<Button>(R.id.buttonGoogleLogin)
        val progressSignIn = findViewById<ProgressBar>(R.id.progressSignIn)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonSignIn.text = ""
                buttonSignIn.isEnabled = false
                progressSignIn.visibility = View.VISIBLE
            } else {
                buttonSignIn.text = "Sign In"
                buttonSignIn.isEnabled = true
                progressSignIn.visibility = View.GONE
            }
        }

        email.addTextChangedListener {
            if (!Patterns.EMAIL_ADDRESS.matcher(it.toString()).matches()) {
                email.error = "Invalid email format!"
            }
        }

        fun emailValidation(): Boolean {
            val eml = email.text.toString().trim()
            return when {
                eml.isBlank() -> { email.error = "Required field"; false }
                !Patterns.EMAIL_ADDRESS.matcher(eml).matches() -> { email.error = "Invalid email format!"; false }
                else -> { email.error = null; true }
            }
        }

        fun passwordValidation(): Boolean {
            val pwd = password.text.toString().trim()
            return when {
                pwd.isBlank() -> { password.error = "Required field"; false }
                else -> { password.error = null; true }
            }
        }

        val signUp = findViewById<TextView>(R.id.textViewSignUp)
        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            finish()
            startActivity(intent)
        }

        buttonSignIn.setOnClickListener {
            if (!emailValidation() || !passwordValidation()) return@setOnClickListener
            lifecycleScope.launch {
                try {
                    setLoading(true)
                    val loginRequest = LoginRequest(
                        email.text.toString(),
                        password.text.toString()
                    )
                    authService.login(loginRequest)
                    val userId = firebaseAuth.currentUser?.uid!!
                    SnackbarUtil.showSuccess(
                        view = findViewById(android.R.id.content),
                        title = "Login Successful",
                        message = "Please wait while we sync your data"
                    )
                    syncService.syncAll(userId)
                    val homePage = Intent(this@MainActivity, LayoutActivity::class.java)
                    finish()
                    startActivity(homePage)
                } catch (e: FirebaseNetworkException) {
                    SnackbarUtil.showError(
                        view = findViewById(android.R.id.content),
                        title = "Network Error",
                        message = "No Internet Connection"
                    )
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    SnackbarUtil.showError(
                        view = findViewById(android.R.id.content),
                        title = "Login Error",
                        message = "Incorrect Login Credentials"
                    )
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                    SnackbarUtil.showError(
                        view = findViewById(android.R.id.content),
                        title = "Error",
                        message = e.message.toString()
                    )
                } finally {
                    setLoading(false)
                }
            }
        }

        buttonGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        lifecycleScope.launch {
            try {
                firebaseAuth.signInWithCredential(credential).await()
                val user = firebaseAuth.currentUser
                val userId = user?.uid!!

                val userDoc = firebaseFirestore.collection("users").document(userId).get().await()
                if (!userDoc.exists()) {
                    // first time login will save to Firestore
                    firebaseFirestore.collection("users").document(userId).set(
                        mapOf(
                            "id" to userId,
                            "firstName" to (user.displayName?.split(" ")?.firstOrNull() ?: ""),
                            "surName" to (user.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: ""),
                        )
                    ).await()
                }
                SnackbarUtil.showSuccess(
                    view = findViewById(android.R.id.content),
                    title = "Login Successful",
                    message = "Please wait while we sync your data"
                )
                Log.d("user", firebaseAuth.currentUser?.displayName!!)
                syncService.syncAll(userId)
                val homePage = Intent(this@MainActivity, LayoutActivity::class.java)
                finish()
                startActivity(homePage)
            } catch (e: FirebaseNetworkException) {
                SnackbarUtil.showError(
                    view = findViewById(android.R.id.content),
                    title = "Network Error",
                    message = "No Internet Connection"
                )
            } catch (e: Exception) {
                SnackbarUtil.showError(
                    view = findViewById(android.R.id.content),
                    title = "Error",
                    message = "Google sign-in failed: ${e.message}"
                )
            }
        }
    }
}