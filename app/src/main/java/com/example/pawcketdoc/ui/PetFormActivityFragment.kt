package com.example.pawcketdoc.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.Pet
import com.example.pawcketdoc.service.PetService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
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

    private val breedMap = mapOf(
        "Dog" to listOf("Aspin", "Labrador", "Golden Retriever", "Bulldog", "Poodle", "Beagle", "Shih Tzu", "Chihuahua", "Dachshund", "German Shepherd"),
        "Cat" to listOf("Puspin", "Persian", "Siamese", "Maine Coon", "Ragdoll", "British Shorthair", "Sphynx", "Bengal", "Birman"),
        "Bird" to listOf("Parrot", "Cockatiel", "Canary", "Lovebird", "Budgerigar", "Finch", "Macaw"),
        "Rabbit" to listOf("Holland Lop", "Netherland Dwarf", "Mini Rex", "Lionhead", "Angora"),
        "Hamster" to listOf("Syrian", "Dwarf Campbell", "Roborovski", "Chinese"),
        "Guinea Pig" to listOf("American", "Peruvian", "Abyssinian", "Teddy"),
        "Fish" to listOf("Goldfish", "Betta", "Guppy", "Koi", "Angelfish", "Molly"),
        "Turtle" to listOf("Red-Eared Slider", "Box Turtle", "Painted Turtle", "Snapping Turtle"),
        "Snake" to listOf("Ball Python", "Corn Snake", "King Snake", "Boa Constrictor"),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_pet_form, container, false)
        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.surface)
        );
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, ime.bottom)
            insets
        }

        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New pet"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Add new pet to your list"
        subtitle.visibility = View.VISIBLE

        setupDatePicker(view)
        setupTypeDropdown(view)
        setupGenderDropdown(view)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        petService = PetService(db.petDao(), firebaseFirestore)

        val petName = view.findViewById<TextInputEditText>(R.id.etPetName)
        val petType = view.findViewById<AutoCompleteTextView>(R.id.actvPetType)
        val petBreed = view.findViewById<AutoCompleteTextView>(R.id.actvBreed)
        val petGender = view.findViewById<AutoCompleteTextView>(R.id.actvGender)
        val petBirthdate = view.findViewById<TextInputEditText>(R.id.etBirthDate)

        val buttonAddPet = view.findViewById<Button>(R.id.buttonAddPet)
        val progress = view.findViewById<ProgressBar>(R.id.progress)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonAddPet.text = ""
                buttonAddPet.isEnabled = false
                progress.visibility = View.VISIBLE
            } else {
                buttonAddPet.text = "Add to Pets"
                buttonAddPet.isEnabled = true
                progress.visibility = View.GONE
            }
        }

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
                    setLoading(true)
                    petService.insert(newPet)
                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Pet has been added"
                    )
                    findNavController().popBackStack()
                } catch (e: RuntimeException) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = e.message.toString()
                    )
                } catch (e: FirebaseNetworkException) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Network Error",
                        message = "No Internet Connection"
                    )
                } finally {
                    setLoading(false)
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

    private fun setupTypeDropdown(root: View) {
        val types = breedMap.keys.toList()
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        val typeAutoComplete = root.findViewById<AutoCompleteTextView>(R.id.actvPetType)
        val breedAutoComplete = root.findViewById<AutoCompleteTextView>(R.id.actvBreed)

        typeAutoComplete.setAdapter(typeAdapter)

        typeAutoComplete.setOnItemClickListener { _, _, _, _ ->
            val selectedType = typeAutoComplete.text.toString()
            val breeds = breedMap[selectedType] ?: listOf("Mixed / Unknown")

            val breedAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                breeds
            )
            breedAutoComplete.setAdapter(breedAdapter)
            breedAutoComplete.text.clear()
            breedAutoComplete.isEnabled = true
        }
    }

    private fun setupGenderDropdown(root: View) {
        val genders = arrayOf("Male", "Female")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            genders
        )

        val genderAutoComplete = root.findViewById<AutoCompleteTextView>(R.id.actvGender)
        genderAutoComplete.setAdapter(adapter)
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
}