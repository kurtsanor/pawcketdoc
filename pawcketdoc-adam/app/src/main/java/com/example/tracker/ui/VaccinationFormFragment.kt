package com.example.tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tracker.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VaccinationFormFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(android.graphics.Color.WHITE)

        return inflater.inflate(R.layout.activity_vaccination_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New Vaccination"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Record a new vaccination schedule"
        subtitle.visibility = View.VISIBLE

        setupDatePicker(view)

        val buttonSaveVaccination = view.findViewById<Button>(R.id.btnSaveVaccination)

        buttonSaveVaccination.setOnClickListener {
            Toast.makeText(context, "Vaccination Saved!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
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
