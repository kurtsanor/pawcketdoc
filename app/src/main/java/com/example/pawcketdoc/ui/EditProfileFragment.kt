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
import com.example.pawcketdoc.service.UserService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.lang.Exception

class EditProfileFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var userService: UserService
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etSurname: TextInputEditText
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Edit Profile"
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
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, ime.bottom.coerceAtLeast(systemBars.bottom))
            insets
        }
        etFirstName = view.findViewById(R.id.etFirstName)
        etSurname = view.findViewById(R.id.etSurname)
        val btnUpdateProfile = view.findViewById<Button>(R.id.btnUpdateProfile)

        val progress = view.findViewById<ProgressBar>(R.id.progress)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                btnUpdateProfile.text = ""          // hide text
                btnUpdateProfile.isEnabled = false  // prevent double click
                progress.visibility = View.VISIBLE
            } else {
                btnUpdateProfile.text = "Update Profile"
                btnUpdateProfile.isEnabled = true
                progress.visibility = View.GONE
            }
        }


        db = DatabaseProvider.getDatabase(requireContext())
        firebaseFirestore = Firebase.firestore
        firebaseAuth = Firebase.auth
        userService = UserService(db.userDao(), firebaseFirestore)

        val userId = firebaseAuth.currentUser?.uid!!

        populateUserInfo(userId)

        btnUpdateProfile.setOnClickListener {
            if (etFirstName.text.isNullOrBlank()) {
                etFirstName.error = "First name is required"
                return@setOnClickListener
            } else {
                etFirstName.error = null
            }

            if (etSurname.text.isNullOrBlank()) {
                etSurname.error = "Surname is required"
                return@setOnClickListener
            } else {
                etSurname.error = null
            }
            lifecycleScope.launch {
                try {
                    setLoading(true)
                    val oldUser = userService.findById(userId)
                    val updatedUser= oldUser.copy(firstName = etFirstName.text.toString(), surName = etSurname.text.toString())
                    userService.update(updatedUser)
                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Profile has been updated"
                    )
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = e.message.toString()
                    )
                } finally {
                    setLoading(false)
                }

            }
        }
    }

    fun populateUserInfo(userId: String) {
        lifecycleScope.launch {
            try {
                val currentUser = userService.findById(userId)
                etFirstName.setText(currentUser.firstName)
                etSurname.setText(currentUser.surName)
            } catch (e: Exception) {
                SnackbarUtil.showError(
                    view = requireView(),
                    title = "Auth Error",
                    message = "Please log in again"
                )
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = ""
        subtitle.visibility = View.GONE
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