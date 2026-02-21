package com.example.tracker.ui

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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.AppointmentAdapter
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.model.Appointment
import com.example.tracker.service.AppointmentService
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

    private lateinit var db: AppDatabase
    private lateinit var appointmentService: AppointmentService

    private lateinit var recyclerView: RecyclerView

    private lateinit var appointments: LiveData<List<Appointment>>

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

        db = DatabaseProvider.getDatabase(requireContext())
        appointmentService = AppointmentService(db.appointmentDao())

        val donutChart = view.findViewById<PieChart>(R.id.donutChart)
        createDonutChart(donutChart)

        val barChart = view.findViewById<LineChart>(R.id.barChart)
        createLineChart(barChart)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVaccinationHome)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val userId = requireActivity().intent.getLongExtra("USER_ID", -1L)
        loadUpcomingAppointments(userId)
    }

    fun setupPlaceholders (appointments: List<Appointment>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_appointment)
        if (appointments.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadUpcomingAppointments(userId: Long) {
        appointments = appointmentService.findUpcomingByUserId(userId)
        appointments.observe(viewLifecycleOwner) { appointments ->
            recyclerView.adapter = AppointmentAdapter(appointments) { appointment ->
                val bottomSheet = AppointmentDetailsBottomSheet()
                bottomSheet.show(parentFragmentManager, "AppointmentDetailsBottomSheet")
            }
            setupPlaceholders(appointments)
        }
    }

    private fun createLineChart(lineChart: LineChart) {
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 9f))
        entries.add(Entry(1f, 10f))
        entries.add(Entry(2f, 8f))
        entries.add(Entry(3f, 7f))

        val dataSet = LineDataSet(entries, "Pets")

        val accentColor = Color.parseColor("#4da972")

        dataSet.color = accentColor
        dataSet.setCircleColor(accentColor)
        dataSet.circleHoleColor = Color.WHITE // Makes the dots pop
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setDrawCircleHole(true)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.DKGRAY // Keeps text legible

        // Smooth Curved Line styling
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.15f //

        dataSet.setDrawFilled(true)
        dataSet.fillColor = accentColor
        dataSet.fillAlpha = 65

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Chart Configuration
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.xAxis.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisRight.setDrawGridLines(false)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.axisLeft.setDrawAxisLine(false)

        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.isEnabled = false

        lineChart.animateY(800, Easing.EaseInOutQuad)

        val labels = listOf("Dog", "Cat", "Bird", "Rabbit")
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = Color.parseColor("#757575")

        lineChart.invalidate()
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

            // Harmonized colors with new primary green
            colors = listOf(
                "#2C9270".toColorInt(), // Healthy - primary green
                "#D9B750".toColorInt(), // Minor Issues - warm yellow
                "#DF6F5F".toColorInt(), // Serious Issues - muted red/orange
                "#A3303B".toColorInt()  // Critical - deep red
            )

            sliceSpace = 4f
            selectionShift = 8f
        }

        donutChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            isRotationEnabled = true
            setDrawEntryLabels(false)

            isDrawHoleEnabled = true
            holeRadius = 70f
            setHoleColor(Color.TRANSPARENT)

            animateY(1400, Easing.EaseInOutQuad)

            val text = "85%\nHealth"
            val ss = SpannableString(text)
            ss.setSpan(StyleSpan(Typeface.BOLD), 0, 3, 0)
            ss.setSpan(RelativeSizeSpan(1.9f), 0, 3, 0)
            ss.setSpan(ForegroundColorSpan(Color.GRAY), 4, text.length, 0)
            ss.setSpan(RelativeSizeSpan(1.0f), 4, text.length, 0)

            centerText = ss
        }

        donutChart.legend.apply {
            isEnabled = true
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 10f
            form = Legend.LegendForm.CIRCLE
            textSize = 12f
        }

        donutChart.invalidate()
    }


}