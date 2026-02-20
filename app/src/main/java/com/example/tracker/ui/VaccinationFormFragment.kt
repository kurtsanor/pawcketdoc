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
import com.example.tracker.model.Vaccination
import com.example.tracker.service.VaccinationService
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)

        return inflater.inflate(R.layout.activity_vaccination_form, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New Vaccination"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Log a past vaccination"
        subtitle.visibility = View.VISIBLE

        setupDatePicker(view)

        db = DatabaseProvider.getDatabase(requireContext())
        vaccinationService = VaccinationService(db.vaccinationDao())

        val etVaccineName = view.findViewById<TextInputEditText>(R.id.etVaccineName)
        val etAdministeredDate = view.findViewById<TextInputEditText>(R.id.etAdministeredDate)
        val buttonSaveVaccination = view.findViewById<Button>(R.id.btnSaveVaccination)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etVaccineNotes)

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
                    Toast.makeText(context, "Date cannot be in the future", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                etAdministeredDate.error = "Invalid date format"
                return@setOnClickListener
            }

            val petId = arguments?.getLong("pet_id", -1L)

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
                    vaccinationService.insert(newVaccination)
                    Toast.makeText(requireContext(), "Record has been added", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } catch (e: RuntimeException) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }

            }
        }
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
