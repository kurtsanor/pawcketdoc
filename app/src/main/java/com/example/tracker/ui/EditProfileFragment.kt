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
import androidx.navigation.fragment.findNavController
import com.example.tracker.R
import com.google.android.material.textfield.TextInputEditText

class EditProfileFragment : Fragment() {
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
        val etFirstName = view.findViewById<TextInputEditText>(R.id.etFirstName)
        val etSurname = view.findViewById<TextInputEditText>(R.id.etSurname)
        val btnUpdateProfile = view.findViewById<Button>(R.id.btnUpdateProfile)

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
            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()

            findNavController().popBackStack()
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