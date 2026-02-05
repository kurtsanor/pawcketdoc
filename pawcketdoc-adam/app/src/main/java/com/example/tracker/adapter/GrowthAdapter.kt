package com.example.tracker.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.AppointmentAdapter.AppointmentViewHolder
import com.example.tracker.model.Growth
import com.example.tracker.util.DateFormatter

class GrowthAdapter(
    private val growthEntries: List<Growth>
) : RecyclerView.Adapter<GrowthAdapter.GrowthViewHolder>(){

    class GrowthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weight: TextView = view.findViewById(R.id.textViewWeight)
        val height: TextView = view.findViewById(R.id.textViewHeight)
        val dateRecorded: TextView = view.findViewById(R.id.textViewDateRecorded)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GrowthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_growth, parent, false)
        return GrowthViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: GrowthViewHolder,
        position: Int
    ) {
        val growth = growthEntries[position]
        holder.weight.text = growth.weight.toString() + " kg"
        holder.height.text = growth.height.toString() + " cm"
        holder.dateRecorded.text = DateFormatter.toShortMonthFormat(growth.dateRecorded)
    }

    override fun getItemCount(): Int {
        return growthEntries.size
    }



}