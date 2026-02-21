package com.example.tracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tracker.R
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.service.UserService
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var userService: UserService

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

        db = DatabaseProvider.getDatabase(requireContext())
        userService = UserService(db.userDao())

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

        val userId = requireActivity().intent.getLongExtra("USER_ID", -1L)
        loadUserProfile(userId)
    }

    private fun loadUserProfile(userId: Long) {
        val fullName = view?.findViewById<TextView>(R.id.fullName)
        lifecycleScope.launch {
            val user = userService.findById(userId)
            fullName?.text = buildString {
                append(user.firstName)
                append(" ")
                append(user.surName)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Account"
    }

}