package com.example.tracker.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.MedicalRecordAdapter.MedicalRecordViewHolder
import com.example.tracker.model.Medication
import com.example.tracker.util.DateFormatter

class MedicationAdapter(
    private val medications: List<Medication>
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val medName: TextView = view.findViewById(R.id.textViewMedName)
        val dateRange: TextView = view.findViewById(R.id.textViewDateRange)
        val reason: TextView = view.findViewById(R.id.textViewReason)
        val dose: TextView = view.findViewById(R.id.textViewDose)
        val frequency: TextView = view.findViewById(R.id.textViewFrequency)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: MedicationViewHolder,
        position: Int
    ) {
        val medication = medications[position]
        holder.medName.text = medication.name
        holder.dateRange.text = DateFormatter.toShortMonthFormat(medication.startDate)  + " - " + DateFormatter.toShortMonthFormat(medication.endDate)
        holder.reason.text = medication.reason
        holder.dose.text = medication.dosage
        holder.frequency.text = medication.frequency
    }

    override fun getItemCount(): Int {
        return medications.size
    }




}