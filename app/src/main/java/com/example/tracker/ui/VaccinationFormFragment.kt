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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tracker.R
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.database.VaccinationEntity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class VaccinationFormFragment : Fragment() {

    private var petId: Long = -1L

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

        //  Get petId passed from VaccinationFragment
        petId = arguments?.getLong("petId", -1L) ?: -1L
        if (petId == -1L) {
            Toast.makeText(requireContext(), "Pet ID missing.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "New Vaccination"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Record a new vaccination schedule"
        subtitle.visibility = View.VISIBLE

        setupDatePicker(view)

        val buttonSaveVaccination = view.findViewById<Button>(R.id.btnSaveVaccination)
        buttonSaveVaccination.setOnClickListener {
            saveVaccinationToRoom(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveVaccinationToRoom(root: View) {
        //  MATCHING YOUR XML IDs
        val name = root.findViewById<TextInputEditText>(R.id.etVaccineName)
            ?.text?.toString()?.trim().orEmpty()

        val notes = root.findViewById<TextInputEditText>(R.id.etVaccineNotes)
            ?.text?.toString()?.trim().orEmpty()

        val dateStr = root.findViewById<TextInputEditText>(R.id.etAdministeredDate)
            ?.text?.toString()?.trim().orEmpty()

        if (name.isBlank() || notes.isBlank() || dateStr.isBlank()) {
            Toast.makeText(requireContext(), "Please complete all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val date = try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(dateStr, formatter)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid date. Please select again.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            db.vaccinationDao().insertVaccination(
                VaccinationEntity(
                    petId = petId,
                    title = name,
                    notes = notes,
                    date = date
                )
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Vaccination Saved!", Toast.LENGTH_SHORT).show()
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
                dateInput.setText(formatter.format(Date(selection)))
            }
        }
    }
}
