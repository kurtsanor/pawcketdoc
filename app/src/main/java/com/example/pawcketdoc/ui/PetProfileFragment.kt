package com.example.pawcketdoc.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.service.PetService
import com.example.pawcketdoc.service.UploadService
import com.example.pawcketdoc.util.DateFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class PetProfileFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var petService: PetService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var uploadService: UploadService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pet_profile, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Pet Profile"
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        petService = PetService(db.petDao(), firebaseFirestore)
        uploadService = UploadService(requireContext())

        val petId = arguments?.getString("pet_id")!!

        val bundle = Bundle().apply {
            putString("pet_id", petId)
        }

        val buttonVaccination = view.findViewById< Button>(R.id.buttonVaccination)
        buttonVaccination.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_vaccinations, bundle)
        }

        val buttonMedicalHistory = view.findViewById< Button>(R.id.buttonMedicalHistory)
        buttonMedicalHistory.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_medicalHistory, bundle)
        }

        val buttonGrowth = view.findViewById< Button>(R.id.buttonGrowth)
        buttonGrowth.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_growth, bundle)
        }

        val buttonDocuments = view.findViewById< Button>(R.id.buttonDocuments)
        buttonDocuments.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_documents, bundle)
        }

        val buttonAppointments = view.findViewById< Button>(R.id.buttonAppointments)
        buttonAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_appointments, bundle)
        }

        val buttonMedications = view.findViewById< Button>(R.id.buttonMedications)
        buttonMedications.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_medications, bundle)
        }

        loadPetProfile(petId)
    }

    private val pickFile = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                val petId = arguments?.getString("pet_id")!!
                try {
                    val url = uploadService.uploadPetImage(it)
                    petService.updatePetAvatar(petId, url["secure_url"]!!, url["public_id"]!!)
                    val profilePic = view?.findViewById<ImageView>(R.id.profilePic)
                    Glide.with(this@PetProfileFragment)
                        .load(url["secure_url"])
                        .into(profilePic!!)
                    view?.let { v ->
                        Snackbar.make(v, "Profile photo updated!", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.accent))
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPetProfile(petId: String) {
        val petName = view?.findViewById<TextView>(R.id.petName)
        val petBreed = view?.findViewById<TextView>(R.id.petBreed)

        val infoPetName = view?.findViewById<TextView>(R.id.infoPetName)
        val infoPetSpecies = view?.findViewById<TextView>(R.id.infoPetSpecies)
        val infoPetBreed = view?.findViewById<TextView>(R.id.infoPetBreed)
        val infoPetGender = view?.findViewById<TextView>(R.id.infoPetGender)
        val infoPetBirthdate = view?.findViewById<TextView>(R.id.infoPetBirthdate)
        val headerGender = view?.findViewById<TextView>(R.id.genderHeader)
        val headerBirthday = view?.findViewById<TextView>(R.id.birthdayHeader)

        val profilePic = view?.findViewById<ImageView>(R.id.profilePic)
        profilePic?.setOnClickListener {
            pickFile.launch("image/*")
        }

        lifecycleScope.launch {
            val pet = petService.findById(petId)
            Glide.with(this@PetProfileFragment)
                .load(pet.avatarUrl)
                .placeholder(R.drawable.pet_placeholder)
                .into(profilePic!!)
            petName?.text = pet.name
            petBreed?.text = pet.breed
            infoPetName?.text = pet.name
            infoPetSpecies?.text = pet.type
            infoPetBreed?.text = pet.breed
            infoPetGender?.text = pet.gender
            infoPetBirthdate?.text = pet.birthDate.toString()
            headerGender?.text = pet.gender
            headerBirthday?.text = buildString {
                append("Born\n")
                append(DateFormatter.toShortMonthFormat(pet.birthDate))
            }
        }
    }

}