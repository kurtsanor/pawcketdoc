package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.tracker.model.Medication
import com.example.tracker.service.MedicationService
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
import java.util.Date
import java.util.Locale

class MedicationFormFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var medicationService: MedicationService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_medication_form, container, false)

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)

        setupDatePickerStart(view)
        setupDatePickerEnd(view)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New Medication"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Record new medication schedule"
        subtitle.visibility = View.VISIBLE

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        medicationService = MedicationService(db.medicationDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        val etMedicationName = view.findViewById<TextInputEditText>(R.id.etMedicationName)
        val etDosage = view.findViewById<TextInputEditText>(R.id.etDosage)
        val etFrequency = view.findViewById<TextInputEditText>(R.id.etFrequency)
        val etStartDate = view.findViewById<TextInputEditText>(R.id.etStartDate)
        val etEndDate = view.findViewById<TextInputEditText>(R.id.etEndDate)
        val etReason = view.findViewById<TextInputEditText>(R.id.etReason)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etNotes)
        val buttonSaveMedication = view.findViewById<Button>(R.id.btnSaveMedication)

        buttonSaveMedication.setOnClickListener {
            if (etMedicationName.text.isNullOrBlank()) {
                etMedicationName.error = "Medication name is required"
                return@setOnClickListener
            } else {
                etMedicationName.error = null
            }

            if (etDosage.text.isNullOrBlank()) {
                etDosage.error = "Dosage is required"
                return@setOnClickListener
            } else {
                etDosage.error = null
            }

            if (etFrequency.text.isNullOrBlank()) {
                etFrequency.error = "Frequency is required"
                return@setOnClickListener
            } else {
                etFrequency.error = null
            }

            if (etStartDate.text.isNullOrBlank()) {
                etStartDate.error = "Start date is required"
                return@setOnClickListener
            } else {
                etStartDate.error = null
            }

            if (etEndDate.text.isNullOrBlank()) {
                etEndDate.error = "End date is required"
                return@setOnClickListener
            } else {
                etEndDate.error = null
            }

            // Validate End Date is after Start Date
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val startDate = dateFormat.parse(etStartDate.text.toString())
                val endDate = dateFormat.parse(etEndDate.text.toString())

                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    etEndDate.error = "End date must be after start date"
                    Toast.makeText(context, "End date must be after start date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                etEndDate.error = "Invalid date format"
                return@setOnClickListener
            }

            if (etReason.text.isNullOrBlank()) {
                etReason.error = "Reason is required"
                return@setOnClickListener
            } else {
                etReason.error = null
            }

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            val newMedication = Medication(
                petId = petId,
                name = etMedicationName.text.toString(),
                dosage = etDosage.text.toString(),
                frequency = etFrequency.text.toString(),
                startDate = LocalDate.parse(etStartDate.text.toString(), formatter),
                endDate = LocalDate.parse(etEndDate.text.toString(), formatter),
                reason = etReason.text.toString(),
                notes = etNotes.text.toString()
            )

            lifecycleScope.launch {
                try {
                    medicationService.insert(newMedication)
                    Toast.makeText(context, "Medication Saved!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
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

    private fun setupDatePickerStart(root: View) {
        val dateInput = root.findViewById<TextInputEditText>(R.id.etStartDate)

        dateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Start Date")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(requireActivity().supportFragmentManager, "start_date_picker")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = formatter.format(Date(selection))
                dateInput.setText(dateString)
            }
        }
    }

    private fun setupDatePickerEnd(root: View) {
        val dateInput = root.findViewById<TextInputEditText>(R.id.etEndDate)

        dateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select End Date")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(requireActivity().supportFragmentManager, "end_date_picker")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = formatter.format(Date(selection))
                dateInput.setText(dateString)
            }
        }
    }

}