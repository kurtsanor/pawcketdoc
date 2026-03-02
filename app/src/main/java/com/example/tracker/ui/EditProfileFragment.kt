package com.example.tracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tracker.R
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.service.UserService
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.lang.Exception

class EditProfileFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var userService: UserService
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etSurname: TextInputEditText

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
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etFirstName = view.findViewById(R.id.etFirstName)
        etSurname = view.findViewById(R.id.etSurname)
        val btnUpdateProfile = view.findViewById<Button>(R.id.btnUpdateProfile)

        val userId = requireActivity().intent.getLongExtra("USER_ID", -1L)

        db = DatabaseProvider.getDatabase(requireContext())
        userService = UserService(db.userDao())

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
                    val oldUser = userService.findById(userId)
                    val updatedUser= oldUser.copy(firstName = etFirstName.text.toString(), surName = etSurname.text.toString())
                    userService.update(updatedUser)
                    Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun populateUserInfo(userId: Long) {
        lifecycleScope.launch {
            val currentUser = userService.findById(userId)
            etFirstName.setText(currentUser.firstName)
            etSurname.setText(currentUser.surName)
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