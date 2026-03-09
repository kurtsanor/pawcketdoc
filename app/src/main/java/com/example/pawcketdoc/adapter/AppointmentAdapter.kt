package com.example.pawcketdoc.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.R
import com.example.pawcketdoc.util.DateFormatter

class AppointmentAdapter(
    private val appointments: List<Appointment>,
    private val onClick: (Appointment) -> Unit
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

        holder.itemView.setOnClickListener { onClick(appointment) }
    }

    override fun getItemCount(): Int {
        return appointments.size
    }



}