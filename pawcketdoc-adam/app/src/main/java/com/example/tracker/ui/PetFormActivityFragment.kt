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
import androidx.fragment.app.Fragment
import com.example.tracker.R
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.database.PetEntity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class PetFormActivityFragment : Fragment() {

    private val dbExecutor = Executors.newSingleThreadExecutor()


    // Later you can replace it with actual logged-in user's ID.
    private val userId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_pet_form, container, false)

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "New pet"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Add new pet to your list"
        subtitle.visibility = View.VISIBLE

        // ✅ Call setup functions (you had them but they were not being called)
        setupDatePicker(view)
        setupGenderDropdown(view)

        val buttonSavePet = view.findViewById<Button>(R.id.buttonAddPet)
        buttonSavePet.setOnClickListener {
            savePetToRoom(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun savePetToRoom(root: View) {
        val name = root.findViewById<TextInputEditText>(R.id.etPetName)?.text?.toString()?.trim().orEmpty()
        val type = root.findViewById<TextInputEditText>(R.id.etPetType)?.text?.toString()?.trim().orEmpty()
        val breed = root.findViewById<TextInputEditText>(R.id.etBreed)?.text?.toString()?.trim().orEmpty()
        val birthDateStr = root.findViewById<TextInputEditText>(R.id.etBirthDate)?.text?.toString()?.trim().orEmpty()
        val gender = root.findViewById<AutoCompleteTextView>(R.id.actvGender)?.text?.toString()?.trim().orEmpty()

        // ✅ Basic validation
        if (name.isBlank() || type.isBlank() || breed.isBlank() || gender.isBlank() || birthDateStr.isBlank()) {
            Toast.makeText(requireContext(), "Please complete all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Parse birth date from dd/MM/yyyy
        val birthDate = try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(birthDateStr, formatter)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid birth date. Please select again.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(requireContext())

        dbExecutor.execute {
            db.petDao().insertPet(
                PetEntity(
                    userId = userId,
                    name = name,
                    type = type,
                    breed = breed,
                    gender = gender,
                    birthDate = birthDate
                )
            )

            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "New pet added!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(android.graphics.Color.parseColor("#EFF0F4"))

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

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            genders
        )

        val genderAutoComplete = root.findViewById<AutoCompleteTextView>(R.id.actvGender)
        genderAutoComplete.setAdapter(adapter)
    }
}
