package com.example.tracker.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.GrowthAdapter.GrowthViewHolder
import com.example.tracker.model.MedicalRecord
import com.example.tracker.util.DateFormatter

class MedicalRecordAdapter(
    val medicalRecords: List<MedicalRecord>
) : RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder>() {

    class MedicalRecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.medicalTitle)
        val diagnosis: TextView = view.findViewById(R.id.medicalDiagnosis)
        val treatment: TextView = view.findViewById(R.id.medicalTreatment)
        val date: TextView = view.findViewById(R.id.medicalDate)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedicalRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical, parent, false)
        return MedicalRecordViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: MedicalRecordViewHolder,
        position: Int
    ) {
        val medicalRecord = medicalRecords[position]
        holder.title.text = medicalRecord.title
        ("Diagnosis: " + medicalRecord.diagnosis).also { holder.diagnosis.text = it }
        ("Treatment: " + medicalRecord.treatment).also { holder.treatment.text = it }
        holder.date.text = DateFormatter.toShortMonthFormat(medicalRecord.date)
    }

    override fun getItemCount(): Int {
        return medicalRecords.size
    }



}