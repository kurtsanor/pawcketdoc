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
import com.example.tracker.adapter.MedicalRecordAdapter
import com.example.tracker.adapter.VaccinationAdapter
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.model.MedicalRecord
import com.example.tracker.model.Vaccination
import com.example.tracker.service.MedicalRecordService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.time.LocalDate

class MedicalHistoryFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var medicalRecordService: MedicalRecordService
    private lateinit var recyclerView: RecyclerView
    private lateinit var medicalRecords: LiveData<List<MedicalRecord>>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medical_history, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Medical History"
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
        medicalRecordService = MedicalRecordService(db.medicalRecordDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMedical)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val fabAddMedical = view.findViewById<FloatingActionButton>(R.id.fab_add_medical)

        val bundle = Bundle().apply {
            putString("pet_id", petId)
        }

        fabAddMedical.setOnClickListener {
            findNavController().navigate(R.id.action_medicalHistory_to_medicalHistoryForm, bundle)
        }

        loadMedicalRecords(petId)
        setupSwipeHandler()
    }

    fun setupPlaceholders (medicalRecords: List<MedicalRecord>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_medical_record)
        if (medicalRecords.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun loadMedicalRecords(petId: String) {
        medicalRecords = medicalRecordService.findAllByPetId(petId)
        medicalRecords.observe(viewLifecycleOwner) { medicalRecords ->
            recyclerView.adapter = MedicalRecordAdapter(medicalRecords) { record ->
                val bottomSheet = MedicalRecordBottomSheet.newInstance(record)
                bottomSheet.show(parentFragmentManager, "MedicalDetailsBottomSheet")
            }
            setupPlaceholders(medicalRecords)
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
                val record = medicalRecords.value?.get(position)

                if (record == null) {
                    return
                }


                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete ${record?.title}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                medicalRecordService.deleteById(record.id)
                                Toast.makeText(requireContext(), "${record?.title} deleted", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Swiped id ${record?.id} to $dir", Toast.LENGTH_SHORT).show()

            }
        }
        // Attach the swipe handler to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


}