package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.tracker.R
import com.example.tracker.model.Appointment
import com.example.tracker.util.DateFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDateTime

class AppointmentDetailsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(appointment: Appointment): AppointmentDetailsBottomSheet {
            return AppointmentDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("id", appointment.id)
                    putString("petId", appointment.petId)
                    putString("title", appointment.title)
                    putString("notes", appointment.notes)
                    putString("location", appointment.location)
                    putString("datetime", appointment.datetime.toString())
                    putString("status", appointment.status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_appointment_details_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        val rawDate: LocalDateTime = LocalDateTime.parse(args.getString("datetime"))
        val formattedDate = DateFormatter.toShortMonthFormat(rawDate)

        view.findViewById<TextView>(R.id.bsTitle).text = args.getString("title")
        view.findViewById<TextView>(R.id.bsDateTime).text = formattedDate
        view.findViewById<TextView>(R.id.bsStatus).text = args.getString("status")
        view.findViewById<TextView>(R.id.bsLocation).text = args.getString("location")
        view.findViewById<TextView>(R.id.bsNotes).text = args.getString("notes")


    }

}