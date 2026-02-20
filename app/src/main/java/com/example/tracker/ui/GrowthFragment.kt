package com.example.tracker.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.model.Vaccination
import com.example.tracker.service.GrowthService
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import com.google.android.material.R as MaterialR


class GrowthFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var growthService: GrowthService
    private lateinit var recyclerView: RecyclerView
    private lateinit var growthList: LiveData<List<Growth>>

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
        growthService = GrowthService(db.growthDao())

        val petId = arguments?.getLong("pet_id", -1L) ?: -1L

        val weightLayout = view.findViewById<TextInputLayout>(R.id.weightLayout)
        val heightLayout = view.findViewById<TextInputLayout>(R.id.heightLayout)
        val notesLayout = view.findViewById<TextInputLayout>(R.id.notesLayout)
        val weightInput = view.findViewById<TextInputEditText>(R.id.weightInput)
        val heightInput = view.findViewById<TextInputEditText>(R.id.heightInput)
        val notesInput = view.findViewById<TextInputEditText>(R.id.notesInput)
        val buttonSaveRecord = view.findViewById<Button>(R.id.buttonSaveRecord)

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
                    growthService.insert(newEntry)
                    clearFields()
                    Toast.makeText(context, "Record Saved!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception){
                    Log.d("error", e.toString())
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                }

            }

        }

        weightLayout.setEndIconTooltip("Weight in kilograms e.g '19.4'")
        heightLayout.setEndIconTooltip("Height in inches e.g '23.1'")
        notesLayout.setEndIconTooltip("Any notes e.g personality change etc")

        val barChart = view.findViewById<LineChart>(R.id.barChart)
        createLineChart(barChart)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewGrowth)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        loadGrowthEntries(petId)
        setupSwipeHandler()
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

    private fun loadGrowthEntries(petId: Long) {
        growthList = growthService.findAllByPetId(petId)
        growthList.observe(viewLifecycleOwner) { growths ->
            recyclerView.adapter = GrowthAdapter(growths) { growth ->
                val bottomSheet = GrowthDetailsBottomSheet()
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
        dataSet.color = "#2C9270".toColorInt() // Line color
        dataSet.setCircleColor("#2C9270".toColorInt()) // Dot color
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setDrawCircleHole(true)
        dataSet.valueTextSize = 12f

        // 3. Modern Look: Smoothing and Filling
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Makes the line curved
        dataSet.setDrawFilled(true)
        dataSet.fillColor = "#A7D8C3".toColorInt()
        dataSet.fillAlpha = 100 // Transparency (0-255)

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
