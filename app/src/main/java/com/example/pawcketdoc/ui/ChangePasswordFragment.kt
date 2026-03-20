package com.example.pawcketdoc.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.service.AuthService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ChangePasswordFragment : Fragment() {
    private lateinit var db: AppDatabase
    private lateinit var authService: AuthService
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Change Password"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.surface)
        );
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }
    override fun onDestroyView() {
        super.onDestroyView()

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = ""
        subtitle.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, ime.bottom.coerceAtLeast(systemBars.bottom))
            insets
        }

        val etCurrentPassword = view.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        authService = AuthService(db.userDao(), db.credentialsDao(), firebaseAuth, firebaseFirestore)

        val progress = view.findViewById<ProgressBar>(R.id.progress)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                btnChangePassword.text = ""          // hide text
                btnChangePassword.isEnabled = false  // prevent double click
                progress.visibility = View.VISIBLE
            } else {
                btnChangePassword.text = "Change Password"
                btnChangePassword.isEnabled = true
                progress.visibility = View.GONE
            }
        }

        btnChangePassword.setOnClickListener {
            val current = etCurrentPassword.text.toString()
            val new = etNewPassword.text.toString()
            val confirm = etConfirmPassword.text.toString()

            // Validate Current Password
            if (current.isBlank()) {
                etCurrentPassword.error = "Current password is required"
                return@setOnClickListener
            }

            // Validate New Password Length
            if (new.length < 6) {
                etNewPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // Validate Confirmation
            if (new != confirm) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    setLoading(true)
                    val currentPassword = etCurrentPassword.text.toString()
                    val user = firebaseAuth.currentUser
                    val credential = EmailAuthProvider.getCredential(user?.email!!, currentPassword)

                    user.reauthenticate(credential).await()

                    val newPassword = etNewPassword.text.toString()
                    authService.changeUserPassword(newPassword)
                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Your Password has been updated"
                    )
                    findNavController().popBackStack()
                } catch (e: FirebaseAuthRecentLoginRequiredException) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Session Expired",
                        message = "Please log in again"
                    )
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = "Current password is incorrect"
                    )
                } catch (e: FirebaseAuthWeakPasswordException) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = "New Password is too weak"
                    )
                } catch (e: Exception) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = "Something went wrong"
                    )
                } finally {
                    setLoading(false)
                }

            }
        }
    }


    override fun onResume() {
        super.onResume()
        // Optional: hide bottom nav like MedicationForm
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        // Optional: show bottom nav again
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }
}