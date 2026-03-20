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
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.service.AppointmentService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AppointmentFormFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var appointmentService: AppointmentService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_appointment_form, container, false)

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

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Book Appointment"

        val subtitle = requireActivity().findViewById<TextView>(R.id.txtHeaderSubtitle)
        subtitle.text = "Schedule a visit for your pet."
        subtitle.visibility = View.VISIBLE

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        appointmentService = AppointmentService(db.appointmentDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        val etAppointmentTitle = view.findViewById<TextInputEditText>(R.id.etAppointmentTitle)
        val etAppointmentLocation = view.findViewById<TextInputEditText>(R.id.etAppointmentLocation)
        val etAppointmentDateTime = view.findViewById<TextInputEditText>(R.id.etAppointmentDateTime)
        val etAppointmentNotes = view.findViewById<TextInputEditText>(R.id.etAppointmentNotes)
        val buttonSaveAppointment = view.findViewById<Button>(R.id.btnSaveAppointment)

        val progress = view.findViewById<ProgressBar>(R.id.progress)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonSaveAppointment.text = ""          // hide text
                buttonSaveAppointment.isEnabled = false  // prevent double click
                progress.visibility = View.VISIBLE
            } else {
                buttonSaveAppointment.text = "Schedule Appointment"
                buttonSaveAppointment.isEnabled = true
                progress.visibility = View.GONE
            }
        }

        buttonSaveAppointment.setOnClickListener {
            if (etAppointmentTitle.text.isNullOrBlank()) {
                etAppointmentTitle.error = "Appointment title is required"
                return@setOnClickListener
            } else {
                etAppointmentTitle.error = null
            }

            if (etAppointmentLocation.text.isNullOrBlank()) {
                etAppointmentLocation.error = "Location is required"
                return@setOnClickListener
            } else {
                etAppointmentLocation.error = null
            }

            if (etAppointmentDateTime.text.isNullOrBlank()) {
                etAppointmentDateTime.error = "Date and time is required"
                return@setOnClickListener
            } else {
                etAppointmentDateTime.error = null
            }

            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a")
                val selectedDateTime = LocalDateTime.parse(etAppointmentDateTime.text.toString(), formatter)
                val currentDateTime = LocalDateTime.now()

                if (selectedDateTime.isBefore(currentDateTime)) {
                    etAppointmentDateTime.error = "Appointment cannot be in the past"
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = "Appointment Cannot be in the past"
                    )
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                etAppointmentDateTime.error = "Invalid date/time format"
                return@setOnClickListener
            }

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a")

            val newAppointment = Appointment(
                petId = petId,
                title = etAppointmentTitle.text.toString(),
                location = etAppointmentLocation.text.toString(),
                notes = etAppointmentNotes.text.toString(),
                status = "Confirmed",
                datetime = LocalDateTime.parse(etAppointmentDateTime.text.toString(), formatter)
            )

            lifecycleScope.launch {
                try {
                    setLoading(true)
                    appointmentService.insert(newAppointment)
                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Appointment Set!"
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
        setupDateTimePicker(view)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDateTimePicker(root: View) {
        val dateInput = root.findViewById<TextInputEditText>(R.id.etAppointmentDateTime)

        dateInput.setOnClickListener {

            // 1. Date Picker
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Appointment Date")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "DatePicker")

            datePicker.addOnPositiveButtonClickListener { selection ->

                // Convert timestamp to date
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selection

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // 2. Time Picker
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(9)
                    .setMinute(0)
                    .setTitleText("Select Time")
                    .setTheme(R.style.CustomTimePickerTheme)
                    .build()

                timePicker.show(parentFragmentManager, "TimePicker")

                timePicker.addOnPositiveButtonClickListener {

                    val hour = timePicker.hour
                    val minute = timePicker.minute

                    // 3. Convert to LocalDateTime
                    val selectedDateTime = LocalDateTime.of(year, month, day, hour, minute)

                    // 4. Format for display (UI only)
                    val uiFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a")
                    val formatted = selectedDateTime.format(uiFormat)

                    // 5. Display in text field
                    dateInput.setText(formatted)
                }
            }
        }
    }
}