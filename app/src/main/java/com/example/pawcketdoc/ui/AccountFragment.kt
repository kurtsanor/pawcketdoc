package com.example.pawcketdoc.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.service.UserService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AccountFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var userService: UserService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

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
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        userService = UserService(db.userDao(), firebaseFirestore)

        val buttonLogout = view.findViewById<TextView>(R.id.logout)

        buttonLogout.setOnClickListener {
            firebaseAuth.signOut()
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

        val userId = firebaseAuth.currentUser?.uid!!
        loadUserProfile(userId)
    }

    private fun loadUserProfile(userId: String) {
        val fullName = view?.findViewById<TextView>(R.id.fullName)
        try {
            val user = userService.findByIdLiveData(userId)
            user.observe(viewLifecycleOwner) { user ->
                if (user == null) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Session Expired",
                        message = "Please log in again"
                    )
                    return@observe
                }
                fullName?.text = buildString {
                    append(user.firstName)
                    append(" ")
                    append(user.surName)
                }
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
            SnackbarUtil.showError(
                view = requireView(),
                title = "Session Expired",
                message = "Please log in again"
            )
        }

    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Account"
    }

}