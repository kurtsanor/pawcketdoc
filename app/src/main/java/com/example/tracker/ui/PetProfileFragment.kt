package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_CLOSE
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tracker.R
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.service.PetService
import com.example.tracker.util.DateFormatter
import kotlinx.coroutines.launch

class PetProfileFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var petService: PetService

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

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseProvider.getDatabase(requireContext())
        petService = PetService(db.petDao())

        val petId = arguments?.getLong("pet_id", -1L) ?: -1L

        val bundle = Bundle().apply {
            putLong("pet_id", petId)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPetProfile(petId: Long) {
        val petName = view?.findViewById<TextView>(R.id.petName)
        val petBreed = view?.findViewById<TextView>(R.id.petBreed)

        val infoPetName = view?.findViewById<TextView>(R.id.infoPetName)
        val infoPetSpecies = view?.findViewById<TextView>(R.id.infoPetSpecies)
        val infoPetBreed = view?.findViewById<TextView>(R.id.infoPetBreed)
        val infoPetGender = view?.findViewById<TextView>(R.id.infoPetGender)
        val infoPetBirthdate = view?.findViewById<TextView>(R.id.infoPetBirthdate)
        val headerGender = view?.findViewById<TextView>(R.id.genderHeader)
        val headerBirthday = view?.findViewById<TextView>(R.id.birthdayHeader)

        lifecycleScope.launch {
            val pet = petService.findById(petId)
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