package com.example.tracker.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.model.Appointment
import com.example.tracker.R
import com.example.tracker.adapter.PetAdapter.PetViewHolder
import com.example.tracker.util.DateFormatter

class AppointmentAdapter(
    private val appointments: List<Appointment>
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.appointmentTitle)
        val datetime: TextView = view.findViewById(R.id.appointmentDateTime)
        val status: TextView = view.findViewById(R.id.appointmentStatus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: AppointmentViewHolder,
        position: Int
    ) {
        val appointment = appointments[position]
        holder.title.text = appointment.title
        holder.datetime.text = DateFormatter.toShortMonthFormat(appointment.datetime)
        holder.status.text = appointment.status
    }

    override fun getItemCount(): Int {
        return appointments.size
    }



}