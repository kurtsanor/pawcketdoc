package com.example.tracker.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.AppointmentAdapter
import com.example.tracker.model.Appointment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.time.LocalDateTime


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerTitle = requireActivity().findViewById<TextView>(R.id.txtHeaderTitle)

        headerTitle.text = "Dashboard"

        headerTitle.setOnClickListener {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        val donutChart = view.findViewById<PieChart>(R.id.donutChart)
        createDonutChart(donutChart)

        val barChart = view.findViewById<LineChart>(R.id.barChart)
        createLineChart(barChart)

        val mockDate = LocalDateTime.of(2003, 5, 2, 0,0,0,0)

        val appointments = listOf(
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza",mockDate, "Confirmed"),
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza",mockDate, "Confirmed"),
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza",mockDate, "Confirmed"),
            Appointment(null, 1, "Check-up", "Urgent", "123 Plaza",mockDate, "Confirmed"),
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVaccinationHome)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = AppointmentAdapter(appointments)

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
        val labels = listOf("Dog", "Cat", "Bird", "Rabbit")
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        lineChart.invalidate() // Refresh
    }

    private fun createDonutChart(donutChart: PieChart) {
        val entries = listOf(
            PieEntry(40f, "Healthy"),
            PieEntry(30f, "Minor Issues"),
            PieEntry(20f, "Serious Issues"),
            PieEntry(10f, "Critical")
        )

        val dataSet = PieDataSet(entries, "").apply {
            setDrawValues(false)
            colors = listOf(
                "#27634a".toColorInt(),
                "#d9a750".toColorInt(),
                "#df675f".toColorInt(),
                "#a3303b".toColorInt()
            )

            // --- MODERN ENHANCEMENTS ---
            sliceSpace = 4f            // Adds white gaps between slices
            selectionShift = 8f        // Pops the slice out slightly when tapped
        }

        donutChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            isRotationEnabled = true   // Allow users to spin it (feels more interactive)
            setDrawEntryLabels(false)

            // --- HOLE STYLING ---
            isDrawHoleEnabled = true
            holeRadius = 70f           // Thinner ring looks more "premium"
            setHoleColor(Color.TRANSPARENT) // Better for cards with custom backgrounds

            // --- ANIMATION ---
            animateY(1400, Easing.EaseInOutQuad) // Smooth entrance animation

            // --- CENTER TEXT (INTER FONT) ---
            val text = "85%\nHealth"
            val ss = SpannableString(text)
            // Make the percentage big and bold
            ss.setSpan(StyleSpan(Typeface.BOLD), 0, 3, 0)
            ss.setSpan(RelativeSizeSpan(1.9f), 0, 3, 0)
            // Make the "Health" label smaller and light
            ss.setSpan(ForegroundColorSpan(Color.GRAY), 4, text.length, 0)
            ss.setSpan(RelativeSizeSpan(0.9f), 4, text.length, 0)

            centerText = ss
        }

        // --- LEGEND STYLING ---
        donutChart.legend.apply {
            isEnabled = true
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 10f              // Adds space between chart and legend
            form = Legend.LegendForm.CIRCLE // Modern circular legend indicators
            textSize = 12f
        }

        donutChart.invalidate()
    }

}