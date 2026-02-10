package com.example.tracker.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_CLOSE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.AppointmentAdapter
import com.example.tracker.model.Appointment
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.time.LocalDateTime

class AppointmentFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_appointment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Appointments"

        val calendarView = view.findViewById< MaterialCalendarView>(R.id.calendarAppointments)
        val today = CalendarDay.today()
        calendarView.setDateSelected(today, true)

        val mockDate = LocalDateTime.of(2003, 5, 2, 0,0,0,0)

        val appointments = listOf(
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza", mockDate, "Confirmed"),
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza", mockDate, "Confirmed"),
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza", mockDate, "Confirmed"),
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza", mockDate, "Confirmed"),
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewAppointments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = AppointmentAdapter(appointments)

        val buttonAdd = view.findViewById<Button>(R.id.buttonAddAppointment)

        buttonAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, AppointmentFormFragment())
                .addToBackStack(null)
                .commit()

        }
    }
}