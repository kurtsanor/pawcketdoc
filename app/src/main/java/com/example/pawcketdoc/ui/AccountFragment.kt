package com.example.pawcketdoc.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.service.UploadService
import com.example.pawcketdoc.service.UserService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var userService: UserService
    private lateinit var uploadService: UploadService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val userAvatar = view?.findViewById<ImageView>(R.id.userAvatar)
            val uploadOverlay = view?.findViewById<FrameLayout>(R.id.uploadOverlay)

            lifecycleScope.launch {
                try {
                    Glide.with(this@AccountFragment).clear(userAvatar!!)
                    uploadOverlay?.visibility = View.VISIBLE
                    userAvatar.isEnabled = false

                    val url = uploadService.uploadUserAvatar(it)

                    val userId = firebaseAuth.currentUser?.uid!!

                    userService.updateAvatar(userId, url["secure_url"]!!, url["public_id"]!!)

                    Glide.with(this@AccountFragment)
                        .load(url["secure_url"])
                        .circleCrop()
                        .into(userAvatar)

                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Avatar has been updated"
                    )
                } catch (e: Exception) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = "Failed to upload avatar"
                    )
                } finally {
                    uploadOverlay?.visibility = View.GONE
                    userAvatar?.isEnabled = true
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        uploadService = UploadService(requireContext())

        val buttonLogout = view.findViewById<TextView>(R.id.logout)
        buttonLogout.setOnClickListener {
            firebaseAuth.signOut()
            requireActivity().finish()
            val loginPage = Intent(requireContext(), MainActivity::class.java)
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

        val userAvatar = view.findViewById<ImageView>(R.id.userAvatar)
        userAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        val userId = firebaseAuth.currentUser?.uid!!
        loadUserProfile(userId)
    }

    private fun loadUserProfile(userId: String) {
        val fullName = view?.findViewById<TextView>(R.id.fullName)
        val userAvatar = view?.findViewById<ImageView>(R.id.userAvatar)

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
                fullName?.text = "${user.firstName} ${user.surName}"

                if (!user.avatarUrl.isNullOrBlank()) {
                    Glide.with(this)
                        .load(user.avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.placeholder_profile)
                        .into(userAvatar!!)
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