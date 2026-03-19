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
import com.example.pawcketdoc.model.Medication
import com.example.pawcketdoc.util.DateFormatter
import com.example.pawcketdoc.util.SwipeDeleteHelper

class MedicationAdapter(
    private val medications: List<Medication>,
    private val onClick: (Medication) -> Unit,
    private val onDeleteClick: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    private val swipeDeleteHelper = SwipeDeleteHelper()

    class MedicationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val swipeLayout: SwipeRevealLayout = view.findViewById(R.id.swipeRevealLayout)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val frontCard: View = view.findViewById(R.id.frontCard)
        val medName: TextView = view.findViewById(R.id.textViewMedName)
        val dateRange: TextView = view.findViewById(R.id.textViewDateRange)
        val reason: TextView = view.findViewById(R.id.textViewReason)
        val dose: TextView = view.findViewById(R.id.textViewDose)
        val frequency: TextView = view.findViewById(R.id.textViewFrequency)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medications[position]
        swipeDeleteHelper.bind(holder.swipeLayout, medication.id)
        holder.medName.text = medication.name
        holder.dateRange.text = buildString {
            append(DateFormatter.toShortMonthFormat(medication.startDate))
            append(" - ")
            append(DateFormatter.toShortMonthFormat(medication.endDate))
        }
        holder.reason.text = medication.reason
        holder.dose.text = medication.dosage
        holder.frequency.text = medication.frequency

        holder.frontCard.setOnClickListener { onClick(medication) }
        holder.btnDelete.setOnClickListener {
            swipeDeleteHelper.close(medication.id)
            onDeleteClick(medication)
        }
    }

    override fun getItemCount(): Int = medications.size
}
