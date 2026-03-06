package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tracker.R
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.model.Pet
import com.example.tracker.service.PetService
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class PetFormActivityFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var petService: PetService

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // IMPORTANT: keep using your existing XML
        val view = inflater.inflate(R.layout.activity_pet_form, container, false)

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New pet"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Add new pet to your list"
        subtitle.visibility = View.VISIBLE

        setupDatePicker(view)
        setupGenderDropdown(view)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        petService = PetService(db.petDao(), firebaseFirestore)

        val petName = view.findViewById<TextInputEditText>(R.id.etPetName)
        val petType = view.findViewById<TextInputEditText>(R.id.etPetType)
        val petBreed = view.findViewById<TextInputEditText>(R.id.etBreed)
        val petGender = view.findViewById<AutoCompleteTextView>(R.id.actvGender)
        val petBirthdate = view.findViewById<TextInputEditText>(R.id.etBirthDate)

        val buttonAddPet = view.findViewById<Button>(R.id.buttonAddPet)
        buttonAddPet.setOnClickListener {
            if (petName.text.isNullOrBlank()) {
                petName.error = "Pet name is required"
                return@setOnClickListener
            } else {
                petName.error = null
            }

            if (petType.text.isNullOrBlank()) {
                petType.error = "Pet type is required"
                return@setOnClickListener
            } else {
                petType.error = null
            }
            if (petBreed.text.isNullOrBlank()) {
                petBreed.error = "Breed is required"
                return@setOnClickListener
            } else {
                petBreed.error = null
            }
            if (petGender.text.isNullOrBlank()) {
                petGender.error = "Gender is required"
                return@setOnClickListener
            } else {
                petGender.error = null
            }
            if (petBirthdate.text.isNullOrBlank()) {
                petBirthdate.error = "Birthdate is required"
                return@setOnClickListener
            } else {
                petBirthdate.error = null
            }

            lifecycleScope.launch {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val newPet = Pet(
                    userId = firebaseAuth.currentUser?.uid!!,
                    name = petName.text.toString(),
                    type = petType.text.toString(),
                    breed = petBreed.text.toString(),
                    gender = petGender.text.toString(),
                    birthDate = LocalDate.parse(petBirthdate.text.toString(), formatter)
                )
                try {
                    petService.insert(newPet)
                    Toast.makeText(requireContext(), "Pet has been added", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } catch (e: RuntimeException) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
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
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    private fun setupDatePicker(root: View) {
        val dateInput = root.findViewById<TextInputEditText>(R.id.etBirthDate)

        dateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Birth Date")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "BIRTH_DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = formatter.format(Date(selection))
                dateInput.setText(dateString)
            }
        }
    }

    private fun setupGenderDropdown(root: View) {
        val genders = arrayOf("Male", "Female", "Other")

        // In fragments, use requireContext() instead of "this"
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            genders
        )

        val genderAutoComplete = root.findViewById<AutoCompleteTextView>(R.id.actvGender)
        genderAutoComplete.setAdapter(adapter)
    }
}
