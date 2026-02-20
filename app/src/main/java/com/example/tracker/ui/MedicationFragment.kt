package com.example.tracker.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.AppointmentAdapter
import com.example.tracker.adapter.MedicalRecordAdapter
import com.example.tracker.adapter.MedicationAdapter
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.model.Appointment
import com.example.tracker.model.Medication
import com.example.tracker.service.AppointmentService
import com.example.tracker.service.MedicationService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.time.LocalDate

class MedicationFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var medicationService: MedicationService
    private lateinit var recyclerView: RecyclerView
    private lateinit var medications: LiveData<List<Medication>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medication, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Medications"
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
        medicationService = MedicationService(db.medicationDao())

        val petId = arguments?.getLong("pet_id", -1L) ?: -1L

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMedications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_medication)

        val bundle = Bundle().apply {
            putLong("pet_id", petId)
        }

        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_medication_to_medicationForm, bundle)
        }
        loadMedications(petId)
        setupSwipeHandler()
    }

    fun setupPlaceholders (medications: List<Medication>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_medication)
        if (medications.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun loadMedications(petId: Long) {
        medications = medicationService.findAllByPetId(petId)
        medications.observe(viewLifecycleOwner) { medications ->
            recyclerView.adapter = MedicationAdapter(medications, {medication ->
                val bottomSheet = MedicationDetailsBottomSheet()
                bottomSheet.show(parentFragmentManager, "MedicationDetailsBottomSheet")
            })
            setupPlaceholders(medications)
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
                val medication = medications.value?.get(position)

                if (medication == null) {
                    return
                }


                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete ${medication?.name}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                medicationService.deleteById(medication.id)
                                Toast.makeText(requireContext(), "${medication?.name} deleted", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Swiped id ${medication?.id} to $dir", Toast.LENGTH_SHORT).show()

            }
        }
        // Attach the swipe handler to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}