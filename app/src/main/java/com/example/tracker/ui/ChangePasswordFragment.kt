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

class ChangePasswordFragment : Fragment() {
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
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)
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
        val etCurrentPassword = view.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)

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

            // Success
            Toast.makeText(context, "Password Changed Successfully!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
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