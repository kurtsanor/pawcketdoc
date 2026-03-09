package com.example.pawcketdoc.ui

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.AppointmentAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.dto.AppointmentMonthCount
import com.example.pawcketdoc.dto.HealthAnalytics
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.service.AppointmentService
import com.example.pawcketdoc.service.MedicationService
import com.example.pawcketdoc.util.HealthUtil
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.LocalDate


class HomeFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var appointmentService: AppointmentService
    private lateinit var medicationService: MedicationService
    private lateinit var recyclerView: RecyclerView
    private lateinit var appointments: LiveData<List<Appointment>>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

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
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        appointmentService = AppointmentService(db.appointmentDao(), firebaseFirestore, firebaseAuth)
        medicationService = MedicationService(db.medicationDao(), firebaseFirestore, firebaseAuth)

        val userId = firebaseAuth.currentUser?.uid

        if (userId.isNullOrBlank()) {
            firebaseAuth.signOut()
            val intent = Intent(requireContext(), MainActivity:: class.java)
            startActivity(intent)
            requireActivity().finish()
            return
        }

        val donutChart = view.findViewById<PieChart>(R.id.donutChart)
        val barChart = view.findViewById<LineChart>(R.id.barChart)
        lifecycleScope.launch {
            val analytics = medicationService.getPetsHealthAnalytics(userId)
            createDonutChart(donutChart, analytics)

            val yearNow = LocalDate.now().year.toString()
            val appointmentEntries = appointmentService.getAppointmentCountsPerMonth(userId, yearNow)
            createLineChart(barChart, appointmentEntries)
        }
        val spinner = view.findViewById<Spinner>(R.id.spinnerYear)
        setupYearDropdown(spinner)
        var isSpinnerReady = false
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isSpinnerReady) {
                    isSpinnerReady = true
                    return
                }
                lifecycleScope.launch {
                    val selectedYear = parent?.getItemAtPosition(position).toString()
                    filterChartByYear(userId, selectedYear, barChart)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVaccinationHome)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        loadUpcomingAppointments(userId)
    }

    private suspend fun filterChartByYear(userId: String, year: String, lineChart: LineChart) {
        val filteredResults = appointmentService.getAppointmentCountsPerMonth(userId, year)

        val entries = ArrayList<Entry>()
        var index = 0f
        for (entry in filteredResults) {
            entries.add(Entry(index, entry.appointmentCount.toFloat()))
            index++
        }

        val dataSet = lineChart.data?.getDataSetByIndex(0) as? LineDataSet
        if (dataSet == null) {
            createLineChart(lineChart, filteredResults)
            return
        }
        dataSet.values = entries
        lineChart.data.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.animateY(500, Easing.EaseInOutQuad)
        lineChart.invalidate()
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
    private fun loadUpcomingAppointments(userId: String) {
        appointments = appointmentService.findUpcomingByUserId(userId)
        appointments.observe(viewLifecycleOwner) { appointments ->
            recyclerView.adapter = AppointmentAdapter(appointments) { appointment ->
                val bottomSheet = AppointmentDetailsBottomSheet.newInstance(appointment)
                bottomSheet.show(parentFragmentManager, "AppointmentDetailsBottomSheet")
            }
            setupPlaceholders(appointments)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupYearDropdown(spinner: Spinner) {
        val currentYear = LocalDate.now().year
        val years = (2023..currentYear).map { it.toString() }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.setSelection(years.size - 1)
    }


    private fun createLineChart(lineChart: LineChart, appointmentEntry: List<AppointmentMonthCount>) {
        val entries = ArrayList<Entry>()
        var index = 0f
        for (entry in appointmentEntry) {
            entries.add(Entry(index, entry.appointmentCount.toFloat()))
            index++
        }

        val dataSet = LineDataSet(entries, "Appointments")

        val accentColor = "#4da972".toColorInt()

        dataSet.color = accentColor
        dataSet.setCircleColor(accentColor)
        dataSet.circleHoleColor = Color.WHITE
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setDrawCircleHole(true)
        dataSet.setDrawValues(false) // remove values beside dots
        dataSet.setDrawCircles(false)

        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.15f

        dataSet.setDrawFilled(true)
        dataSet.fillColor = accentColor
        dataSet.fillAlpha = 35

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        // X axis
        lineChart.xAxis.setDrawGridLines(true)
        lineChart.xAxis.gridColor = Color.parseColor("#E0E0E0")
        lineChart.xAxis.gridLineWidth = 0.5f
        lineChart.xAxis.setDrawAxisLine(false)

        // Left axis (values on the left)
        lineChart.axisLeft.isEnabled = true
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisLeft.gridColor = Color.parseColor("#E0E0E0")
        lineChart.axisLeft.gridLineWidth = 0.5f
        lineChart.axisLeft.setDrawAxisLine(false)
        lineChart.axisLeft.textColor = Color.parseColor("#757575")
        lineChart.axisLeft.granularity = 1f
        lineChart.axisLeft.setDrawZeroLine(false)

        // Disable right axis
        lineChart.axisRight.isEnabled = false

        lineChart.animateY(800, Easing.EaseInOutQuad)

        val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = Color.parseColor("#757575")

        lineChart.invalidate()
    }

    private fun createDonutChart(donutChart: PieChart, healthAnalytics: HealthAnalytics) {
        val entries = listOf(
            PieEntry(healthAnalytics.healthyCount.toFloat(), "Healthy"),
            PieEntry(healthAnalytics.minorIssueCount.toFloat(), "Minor Issues"),
            PieEntry(healthAnalytics.seriousIssueCount.toFloat(), "Serious Issues"),
            PieEntry(healthAnalytics.criticalIssueCount.toFloat(), "Critical")
        )

        val dataSet = PieDataSet(entries, "").apply {
            setDrawValues(false)

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

            val healthyPercentage = HealthUtil.calculateHealthyPercentage(healthAnalytics).toInt()

            val text = "${healthyPercentage}%\nHealthy"
            val ss = SpannableString(text)
            ss.setSpan(StyleSpan(Typeface.BOLD), 0, 3, 0)
            ss.setSpan(RelativeSizeSpan(1.9f), 0, 3, 0)
            ss.setSpan(ForegroundColorSpan(Color.GRAY), 4, text.length, 0)
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