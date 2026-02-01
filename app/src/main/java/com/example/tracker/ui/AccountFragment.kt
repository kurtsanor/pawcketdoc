package com.example.tracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.tracker.R

class AccountFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Account"

        val buttonLogout = view.findViewById<TextView>(R.id.logout)

        buttonLogout.setOnClickListener {
            requireActivity().finish()
            val loginPage = Intent(requireContext(), MainActivity:: class.java)
            startActivity(loginPage)
        }

        val editProfile = view.findViewById<TextView>(R.id.tvEditProfile)
        editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_account_to_editProfile)
        }

        val changePassword = view.findViewById<TextView>(R.id.tvChangePassword)
        changePassword.setOnClickListener {
            findNavController().navigate(R.id.action_account_to_changePassword)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Account"
    }

}