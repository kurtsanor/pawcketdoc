package com.example.tracker.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.model.Vaccination
import com.example.tracker.util.DateFormatter


class VaccinationAdapter(
    private val vaccinations: List<Vaccination>
): RecyclerView.Adapter<VaccinationAdapter.VaccinationViewHolder>() {

    class VaccinationViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val vaccinationName: TextView = view.findViewById(R.id.vaccinationName)
        val vaccinationNotes: TextView = view.findViewById(R.id.vaccinationNotes)
        val vaccinationDate: TextView = view.findViewById(R.id.vaccinationDate)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VaccinationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vaccination, parent, false)
        return VaccinationViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: VaccinationViewHolder,
        position: Int
    ) {
        val vaccination = vaccinations[position]
        holder.vaccinationName.text = vaccination.name
        holder.vaccinationNotes.text = vaccination.notes
        holder.vaccinationDate.text = DateFormatter.toShortMonthFormat(vaccination.administeredDate)
    }

    override fun getItemCount(): Int {
        return vaccinations.size
    }
}