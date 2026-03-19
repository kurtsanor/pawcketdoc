package com.example.pawcketdoc.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.MedicalRecordAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.MedicalRecord
import com.example.pawcketdoc.service.MedicalRecordService
import com.example.pawcketdoc.util.SnackbarUtil
import com.example.pawcketdoc.util.SwipeDeleteHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

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
    ): View {
        return inflater.inflate(R.layout.fragment_medical_history, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Medical History"
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

        recyclerView = view.findViewById(R.id.recyclerViewMedical)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val bundle = Bundle().apply { putString("pet_id", petId) }
        view.findViewById<FloatingActionButton>(R.id.fab_add_medical).setOnClickListener {
            findNavController().navigate(R.id.action_medicalHistory_to_medicalHistoryForm, bundle)
        }

        loadMedicalRecords(petId)
    }

    private fun setupPlaceholders(records: List<MedicalRecord>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_medical_record)
        if (records.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun loadMedicalRecords(petId: String) {
        medicalRecords = medicalRecordService.findAllByPetId(petId)
        medicalRecords.observe(viewLifecycleOwner) { records ->
            recyclerView.adapter = MedicalRecordAdapter(
                medicalRecords = records,
                onClick = { record ->
                    MedicalRecordBottomSheet.newInstance(record)
                        .show(parentFragmentManager, "MedicalDetailsBottomSheet")
                },
                onDeleteClick = { record ->
                    SwipeDeleteHelper.confirmDelete(
                        fragment = this,
                        message = "Are you sure you want to delete ${record.title}?"
                    ) {
                        medicalRecordService.deleteById(record.id)
                        SnackbarUtil.showSuccess(
                            view = requireView(),
                            title = "Success",
                            message = "Record has been deleted"
                        )
                    }
                }
            )
            setupPlaceholders(records)
        }
    }
}
