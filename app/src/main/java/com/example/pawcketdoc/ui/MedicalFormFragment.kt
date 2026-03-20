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
import com.example.pawcketdoc.model.MedicalRecord
import com.example.pawcketdoc.service.MedicalRecordService
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

class MedicalFormFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var medicalRecordService: MedicalRecordService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_medical_form, container, false)

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.surface)
        );

        return  view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, ime.bottom.coerceAtLeast(systemBars.bottom))
            insets
        }

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New Medical Record"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Record medical history."
        subtitle.visibility = View.VISIBLE

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        medicalRecordService = MedicalRecordService(db.medicalRecordDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        val etRecordTitle = view.findViewById<TextInputEditText>(R.id.etRecordTitle)
        val etRecordDate = view.findViewById<TextInputEditText>(R.id.etRecordDate)
        val etDiagnosis = view.findViewById<TextInputEditText>(R.id.etDiagnosis)
        val etTreatment = view.findViewById<TextInputEditText>(R.id.etTreatment)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etRecordNotes)
        val buttonSaveMedical = view.findViewById<Button>(R.id.btnSaveRecord)

        val progress = view.findViewById<ProgressBar>(R.id.progress)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonSaveMedical.text = ""          // hide text
                buttonSaveMedical.isEnabled = false  // prevent double click
                progress.visibility = View.VISIBLE
            } else {
                buttonSaveMedical.text = "Save Medical Record"
                buttonSaveMedical.isEnabled = true
                progress.visibility = View.GONE
            }
        }

        buttonSaveMedical.setOnClickListener {
            if (etRecordTitle.text.isNullOrBlank()) {
                etRecordTitle.error = "Record title is required"
                return@setOnClickListener
            } else {
                etRecordTitle.error = null
            }

            if (etRecordDate.text.isNullOrBlank()) {
                etRecordDate.error = "Date is required"
                return@setOnClickListener
            } else {
                etRecordDate.error = null
            }

            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = dateFormat.parse(etRecordDate.text.toString())
                val currentDate = Calendar.getInstance().time

                if (selectedDate != null && selectedDate.after(currentDate)) {
                    etRecordDate.error = "Date cannot be in the future"
                    Toast.makeText(context, "Date cannot be in the future", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                etRecordDate.error = "Invalid date format"
                return@setOnClickListener
            }

            if (etDiagnosis.text.isNullOrBlank()) {
                etDiagnosis.error = "Diagnosis is required"
                return@setOnClickListener
            } else {
                etDiagnosis.error = null
            }

            if (etTreatment.text.isNullOrBlank()) {
                etTreatment.error = "Treatment is required"
                return@setOnClickListener
            } else {
                etTreatment.error = null
            }

            if (petId == null) {
                Toast.makeText(context, "Missing pet ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            val newRecord = MedicalRecord(
                petId = petId,
                title = etRecordTitle.text.toString(),
                diagnosis = etDiagnosis.text.toString(),
                treatment = etTreatment.text.toString(),
                notes = etNotes.text.toString(),
                date = LocalDate.parse(etRecordDate.text.toString(), formatter)
            )

            lifecycleScope.launch {
                try {
                    setLoading(true)
                    medicalRecordService.insert(newRecord)
                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Record has been saved"
                    )
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = e.message.toString()
                    )
                } finally {
                    setLoading(false)
                }
            }
        }
        setupDatePicker(view)
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
        val dateInput = root.findViewById<TextInputEditText>(R.id.etRecordDate)

        dateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of record")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "RECORD_DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = formatter.format(Date(selection))
                dateInput.setText(dateString)
            }
        }
    }
}
