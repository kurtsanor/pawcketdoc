package com.example.pawcketdoc.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.R
import com.example.pawcketdoc.util.DateFormatter
import com.example.pawcketdoc.util.SwipeDeleteHelper

class AppointmentAdapter(
    private val appointments: List<Appointment>,
    private val onClick: (Appointment) -> Unit,
    private val onDeleteClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    private val swipeDeleteHelper = SwipeDeleteHelper()

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val swipeLayout: SwipeRevealLayout = view.findViewById(R.id.swipeRevealLayout)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val frontCard: View = view.findViewById(R.id.frontCard)
        val title: TextView = view.findViewById(R.id.appointmentTitle)
        val datetime: TextView = view.findViewById(R.id.appointmentDateTime)
        val status: TextView = view.findViewById(R.id.appointmentStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        swipeDeleteHelper.bind(holder.swipeLayout, appointment.id)
        holder.title.text = appointment.title
        holder.datetime.text = DateFormatter.toShortMonthFormat(appointment.datetime)
        holder.status.text = appointment.status

        holder.frontCard.setOnClickListener { onClick(appointment) }
        holder.btnDelete.setOnClickListener {
            swipeDeleteHelper.close(appointment.id)
            onDeleteClick(appointment)
        }
    }

    override fun getItemCount(): Int = appointments.size
}
