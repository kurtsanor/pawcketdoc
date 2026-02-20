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
import com.example.tracker.model.MedicalRecord
import com.example.tracker.service.MedicalRecordService
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_medical_form, container, false)

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)

        return  view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New Medical Record"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Record medical history."
        subtitle.visibility = View.VISIBLE

        db = DatabaseProvider.getDatabase(requireContext())
        medicalRecordService = MedicalRecordService(db.medicalRecordDao())

        val petId = arguments?.getLong("pet_id", -1L)

        val etRecordTitle = view.findViewById<TextInputEditText>(R.id.etRecordTitle)
        val etRecordDate = view.findViewById<TextInputEditText>(R.id.etRecordDate)
        val etDiagnosis = view.findViewById<TextInputEditText>(R.id.etDiagnosis)
        val etTreatment = view.findViewById<TextInputEditText>(R.id.etTreatment)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etRecordNotes)
        val buttonSaveMedical = view.findViewById<Button>(R.id.btnSaveRecord)

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
                    medicalRecordService.insert(newRecord)
                    Toast.makeText(context, "Record Saved!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        setupDatePicker(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))

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
