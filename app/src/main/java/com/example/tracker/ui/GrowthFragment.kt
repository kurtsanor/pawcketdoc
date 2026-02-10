package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.AppointmentAdapter
import com.example.tracker.adapter.GrowthAdapter
import com.example.tracker.model.Growth
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.R as MaterialR


class GrowthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_growth, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Growth"

        val weightLayout = view.findViewById<TextInputLayout>(R.id.weightLayout)
        val heightLayout = view.findViewById<TextInputLayout>(R.id.heightLayout)
        val notesLayout = view.findViewById<TextInputLayout>(R.id.notesLayout)
        val buttonSaveRecord = view.findViewById<Button>(R.id.buttonSaveRecord)

        fun TextInputLayout.setEndIconTooltip(text: CharSequence) {
            val iconView = findViewById<View>(MaterialR.id.text_input_end_icon) ?: return
            TooltipCompat.setTooltipText(iconView, text)

            iconView.setOnClickListener {
                iconView.post { iconView.performLongClick() }
            }
        }

        buttonSaveRecord.setOnClickListener {
            Toast.makeText(context, "Record Saved!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        weightLayout.setEndIconTooltip("Weight in kilograms e.g '19.4'")
        heightLayout.setEndIconTooltip("Height in inches e.g '23.1'")
        notesLayout.setEndIconTooltip("Any notes e.g personality change etc")

        val barChart = view.findViewById<LineChart>(R.id.barChart)
        createLineChart(barChart)

        val growths = listOf(
            Growth(null, 0, 12.4.toFloat(), 14.toFloat(), "Growing", LocalDate.of(2025,1,1)),
            Growth(null, 0, 12.4.toFloat(), 14.toFloat(), "Growing", LocalDate.of(2025,1,1)),
            Growth(null, 0, 12.4.toFloat(), 14.toFloat(), "Growing", LocalDate.of(2025,1,1)),
            Growth(null, 0, 12.4.toFloat(), 14.toFloat(), "Growing", LocalDate.of(2025,1,1)),
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewGrowth)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = GrowthAdapter(growths)


    }

    private fun createLineChart(lineChart: LineChart) {
        // 1. Change BarEntry to Entry
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 9f))
        entries.add(Entry(1f, 10f))
        entries.add(Entry(2f, 8f))
        entries.add(Entry(3f, 7f))

        // 2. Change BarDataSet to LineDataSet
        val dataSet = LineDataSet(entries, "Pets")

        // Styling the line
        dataSet.color = "#08d46c".toColorInt() // Line color
        dataSet.setCircleColor("#08d46c".toColorInt()) // Dot color
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setDrawCircleHole(true)
        dataSet.valueTextSize = 12f

        // 3. Modern Look: Smoothing and Filling
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Makes the line curved
        dataSet.setDrawFilled(true)
        dataSet.fillColor = "#56e49a".toColorInt()
        dataSet.fillAlpha = 50 // Transparency (0-255)

        // 4. Change BarData to LineData
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // 5. Chart Configuration
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        // Remove grid lines and axes lines
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisRight.setDrawGridLines(false)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.axisLeft.setDrawAxisLine(false)
        lineChart.axisRight.isEnabled = false

        // This removes the numbers/labels on the left side
        lineChart.axisLeft.isEnabled = false

        // This removes the actual line on the left side (optional, if not already gone)
        lineChart.axisLeft.setDrawAxisLine(false)

        // --- ANIMATION ---
        lineChart.animateY(700, Easing.EaseInOutQuad) // Smooth entrance animation

        // X-Axis Labels
        val labels = listOf("Jan", "Feb", "Mar", "Apr")
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        lineChart.invalidate() // Refresh
    }
}
