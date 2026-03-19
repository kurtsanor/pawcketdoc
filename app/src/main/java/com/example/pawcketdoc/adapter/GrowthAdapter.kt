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
import com.example.pawcketdoc.model.Growth
import com.example.pawcketdoc.util.DateFormatter
import com.example.pawcketdoc.util.SwipeDeleteHelper

class GrowthAdapter(
    private val growthEntries: List<Growth>,
    private val onClick: (Growth) -> Unit,
    private val onDeleteClick: (Growth) -> Unit
) : RecyclerView.Adapter<GrowthAdapter.GrowthViewHolder>(){

    private val swipeDeleteHelper = SwipeDeleteHelper()

    class GrowthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val swipeLayout: SwipeRevealLayout = view.findViewById(R.id.swipeRevealLayout)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val frontCard: View = view.findViewById(R.id.frontCard)
        val weight: TextView = view.findViewById(R.id.textViewWeight)
        val height: TextView = view.findViewById(R.id.textViewHeight)
        val dateRecorded: TextView = view.findViewById(R.id.textViewDateRecorded)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrowthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_growth, parent, false)
        return GrowthViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: GrowthViewHolder, position: Int) {
        val growth = growthEntries[position]
        swipeDeleteHelper.bind(holder.swipeLayout, growth.id)
        holder.weight.text = growth.weight.toString() + " kg"
        holder.height.text = growth.height.toString() + " cm"
        holder.dateRecorded.text = DateFormatter.toShorterMonthFormat(growth.dateRecorded)

        holder.frontCard.setOnClickListener { onClick(growth) }
        holder.btnDelete.setOnClickListener {
            swipeDeleteHelper.close(growth.id)
            onDeleteClick(growth)
        }
    }

    override fun getItemCount(): Int = growthEntries.size
}
