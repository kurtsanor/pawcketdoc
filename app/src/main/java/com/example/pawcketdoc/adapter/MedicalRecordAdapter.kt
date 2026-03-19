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
import com.example.pawcketdoc.R
import com.example.pawcketdoc.model.MedicalRecord
import com.example.pawcketdoc.util.DateFormatter
import com.example.pawcketdoc.util.SwipeDeleteHelper

class MedicalRecordAdapter(
    val medicalRecords: List<MedicalRecord>,
    private val onClick: (MedicalRecord) -> Unit,
    private val onDeleteClick: (MedicalRecord) -> Unit
) : RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder>() {

    private val swipeDeleteHelper = SwipeDeleteHelper()

    class MedicalRecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val swipeLayout: SwipeRevealLayout = view.findViewById(R.id.swipeRevealLayout)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val frontCard: View = view.findViewById(R.id.frontCard)
        val title: TextView = view.findViewById(R.id.medicalTitle)
        val diagnosis: TextView = view.findViewById(R.id.medicalDiagnosis)
        val treatment: TextView = view.findViewById(R.id.medicalTreatment)
        val date: TextView = view.findViewById(R.id.medicalDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical, parent, false)
        return MedicalRecordViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MedicalRecordViewHolder, position: Int) {
        val medicalRecord = medicalRecords[position]
        swipeDeleteHelper.bind(holder.swipeLayout, medicalRecord.id)
        holder.title.text = medicalRecord.title
        ("Diagnosis: " + medicalRecord.diagnosis).also { holder.diagnosis.text = it }
        ("Treatment: " + medicalRecord.treatment).also { holder.treatment.text = it }
        holder.date.text = DateFormatter.toShortMonthFormat(medicalRecord.date)

        holder.frontCard.setOnClickListener { onClick(medicalRecord) }
        holder.btnDelete.setOnClickListener {
            swipeDeleteHelper.close(medicalRecord.id)
            onDeleteClick(medicalRecord)
        }
    }

    override fun getItemCount(): Int = medicalRecords.size
}
