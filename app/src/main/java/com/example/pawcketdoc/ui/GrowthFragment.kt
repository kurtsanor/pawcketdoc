package com.example.pawcketdoc.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.GrowthAdapter
import com.example.pawcketdoc.model.Growth
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.dto.GrowthProgress
import com.example.pawcketdoc.service.GrowthService
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import com.google.android.material.R as MaterialR


class GrowthFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var growthService: GrowthService
    private lateinit var recyclerView: RecyclerView
    private lateinit var growthList: LiveData<List<Growth>>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_growth, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Growth"
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        growthService = GrowthService(db.growthDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        val weightLayout = view.findViewById<TextInputLayout>(R.id.weightLayout)
        val heightLayout = view.findViewById<TextInputLayout>(R.id.heightLayout)
        val notesLayout = view.findViewById<TextInputLayout>(R.id.notesLayout)
        val weightInput = view.findViewById<TextInputEditText>(R.id.weightInput)
        val heightInput = view.findViewById<TextInputEditText>(R.id.heightInput)
        val notesInput = view.findViewById<TextInputEditText>(R.id.notesInput)
        val buttonSaveRecord = view.findViewById<Button>(R.id.buttonSaveRecord)

        val progress = view.findViewById<ProgressBar>(R.id.progress)
        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonSaveRecord.text = ""          // hide text
                buttonSaveRecord.isEnabled = false  // prevent double click
                progress.visibility = View.VISIBLE
            } else {
                buttonSaveRecord.text = "Save Record"
                buttonSaveRecord.isEnabled = true
                progress.visibility = View.GONE
            }
        }

        fun clearFields() {
            weightInput.setText("")
            heightInput.setText("")
            notesInput.setText("")
        }

        fun TextInputLayout.setEndIconTooltip(text: CharSequence) {
            val iconView = findViewById<View>(MaterialR.id.text_input_end_icon) ?: return
            TooltipCompat.setTooltipText(iconView, text)

            iconView.setOnClickListener {
                iconView.post { iconView.performLongClick() }
            }
        }

        buttonSaveRecord.setOnClickListener {
            // Validate Weight
            if (weightInput.text.isNullOrBlank()) {
                weightInput.error = "Weight is required"
                return@setOnClickListener
            } else {
                weightInput.error = null
            }

            // Validate weight is a valid number and greater than 0
            val weight = weightInput.text.toString().toDoubleOrNull()
            if (weight == null || weight <= 0) {
                weightInput.error = "Please enter a valid weight"
                return@setOnClickListener
            } else {
                weightInput.error = null
            }

            // Validate Height
            if (heightInput.text.isNullOrBlank()) {
                heightInput.error = "Height is required"
                return@setOnClickListener
            } else {
                heightInput.error = null
            }

            // Validate height is a valid number and greater than 0
            val height = heightInput.text.toString().toDoubleOrNull()
            if (height == null || height <= 0) {
                heightInput.error = "Please enter a valid height"
                return@setOnClickListener
            } else {
                heightInput.error = null
            }

            val newEntry = Growth(
                petId = petId,
                weight = weightInput.text.toString().toFloat(),
                height = heightInput.text.toString().toFloat(),
                notes = notesInput.text.toString(),
                dateRecorded = LocalDate.now()
            )
            lifecycleScope.launch {
                try {
                    setLoading(true)
                    growthService.insert(newEntry)
                    clearFields()
                    Toast.makeText(context, "Record Saved!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception){
                    Log.d("error", e.toString())
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                } finally {
                    setLoading(false)
                }

            }

        }

        weightLayout.setEndIconTooltip("Weight in kilograms e.g '19.4'")
        heightLayout.setEndIconTooltip("Height in inches e.g '23.1'")
        notesLayout.setEndIconTooltip("Any notes e.g personality change etc")

        val barChart = view.findViewById<LineChart>(R.id.barChart)
        val yearNow = LocalDate.now().year.toString()
        val weightProgress = growthService.findWeightProgressByYear(petId, yearNow)
        weightProgress.observe(viewLifecycleOwner){ growthProgresses ->
            createLineChart(barChart, growthProgresses)
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
                    filterChartByYear(petId, selectedYear, barChart)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewGrowth)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        loadGrowthEntries(petId)
        setupSwipeHandler()
    }

    private fun filterChartByYear(petId: String, year: String, lineChart: LineChart) {
        val filteredResults = growthService.findWeightProgressByYear(petId, year)
        filteredResults.observe(viewLifecycleOwner) { growthProgresses ->
            val entries = ArrayList<Entry>()
            var index = 0f
            for (entry in growthProgresses) {
                entries.add(Entry(index, entry.averageWeight))
                index++
            }

            val dataSet = lineChart.data?.getDataSetByIndex(0) as? LineDataSet
            if (dataSet == null) {
                createLineChart(lineChart, growthProgresses)
                return@observe
            }
            dataSet.values = entries
            lineChart.data.notifyDataChanged()
            lineChart.notifyDataSetChanged()
            lineChart.animateY(500, Easing.EaseInOutQuad)
            lineChart.invalidate()
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

    fun setupPlaceholders (growthList: List<Growth>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_growth)
        if (growthList.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun loadGrowthEntries(petId: String) {
        growthList = growthService.findAllByPetId(petId)
        growthList.observe(viewLifecycleOwner) { growths ->
            recyclerView.adapter = GrowthAdapter(growths) { growth ->
                val bottomSheet = GrowthDetailsBottomSheet.newInstance(growth)
                bottomSheet.show(parentFragmentManager, "GrowthDetailsBottomSheet")
            }
            setupPlaceholders(growths)
        }
    }

    private fun setupSwipeHandler() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            // drag n drop feature
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Called when an item is swiped
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val growth = growthList.value?.get(position)

                if (growth == null) {
                    return
                }


                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete id ${growth?.id}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                growthService.deleteById(growth.id)
                                Toast.makeText(requireContext(), "id ${growth?.id} deleted", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } catch (e: RuntimeException) {}
                        }

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        // User cancelled, reset the item so it doesn’t disappear
                        recyclerView.adapter?.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()

                // Show a simple toast on swipe
                val dir = if (direction == ItemTouchHelper.LEFT) "left" else "right"
                Toast.makeText(requireContext(), "Swiped id ${growth?.id} to $dir", Toast.LENGTH_SHORT).show()

            }
        }
        // Attach the swipe handler to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun createLineChart(lineChart: LineChart, weightProgress: List<GrowthProgress>) {
        val entries = ArrayList<Entry>()
        var index = 0f
        for (entry in weightProgress) {
            entries.add(Entry(index, entry.averageWeight))
            index++
        }

        val dataSet = LineDataSet(entries, "Weight Progress")

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
}
