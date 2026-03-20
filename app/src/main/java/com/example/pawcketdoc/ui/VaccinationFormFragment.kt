package com.example.pawcketdoc.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.example.pawcketdoc.model.Vaccination
import com.example.pawcketdoc.service.VaccinationService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VaccinationFormFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var vaccinationService: VaccinationService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.surface)
        );

        return inflater.inflate(R.layout.activity_vaccination_form, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, ime.bottom)
            insets
        }

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New Vaccination"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Log a past vaccination"
        subtitle.visibility = View.VISIBLE

        setupDatePicker(view)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        vaccinationService = VaccinationService(db.vaccinationDao(), firebaseFirestore, firebaseAuth)

        val etVaccineName = view.findViewById<TextInputEditText>(R.id.etVaccineName)
        val etAdministeredDate = view.findViewById<TextInputEditText>(R.id.etAdministeredDate)
        val buttonSaveVaccination = view.findViewById<Button>(R.id.btnSaveVaccination)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etVaccineNotes)
        val progress = view.findViewById<ProgressBar>(R.id.progress)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonSaveVaccination.text = ""          // hide text
                buttonSaveVaccination.isEnabled = false  // prevent double click
                progress.visibility = View.VISIBLE
            } else {
                buttonSaveVaccination.text = "Save Record"
                buttonSaveVaccination.isEnabled = true
                progress.visibility = View.GONE
            }
        }

        buttonSaveVaccination.setOnClickListener {
            if (etVaccineName.text.isNullOrBlank()) {
                etVaccineName.error = "Vaccine name is required"
                return@setOnClickListener
            } else {
                etVaccineName.error = null
            }

            if (etAdministeredDate.text.isNullOrBlank()) {
                etAdministeredDate.error = "Date is required"
                return@setOnClickListener
            } else {
                etAdministeredDate.error = null
            }

            // dont allow dates in the future
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = dateFormat.parse(etAdministeredDate.text.toString())
                val currentDate = Calendar.getInstance().time

                if (selectedDate != null && selectedDate.after(currentDate)) {
                    etAdministeredDate.error = "Date cannot be in the future"
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Invalid Date",
                        message = "Date cannot be in the future"
                    )
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                etAdministeredDate.error = "Invalid date format"
                return@setOnClickListener
            }

            val petId = arguments?.getString("pet_id")

            if (petId == null) {
                Toast.makeText(context, "Missing pet ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val newVaccination = Vaccination(
                    petId = petId,
                    name = etVaccineName.text.toString(),
                    notes = etNotes.text.toString(),
                    administeredDate = LocalDate.parse(etAdministeredDate.text.toString(), formatter)
                )
                try {
                    setLoading(true)
                    vaccinationService.insert(newVaccination)
                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Record has been added"
                    )
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
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
        // Optional: hide bottom nav like MedicationForm
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        // Optional: show bottom nav again
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    private fun setupDatePicker(root: View) {
        val dateInput = root.findViewById<TextInputEditText>(R.id.etAdministeredDate)

        dateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Administered Date")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "vaccination_date_picker")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = formatter.format(Date(selection))
                dateInput.setText(dateString)
            }
        }
    }
}
