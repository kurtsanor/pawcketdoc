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
import com.example.pawcketdoc.adapter.MedicationAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.Medication
import com.example.pawcketdoc.service.MedicationService
import com.example.pawcketdoc.util.SnackbarUtil
import com.example.pawcketdoc.util.SwipeDeleteHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MedicationFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var medicationService: MedicationService
    private lateinit var recyclerView: RecyclerView
    private lateinit var medications: LiveData<List<Medication>>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_medication, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Medications"
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
        medicationService = MedicationService(db.medicationDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        recyclerView = view.findViewById(R.id.recyclerViewMedications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val bundle = Bundle().apply { putString("pet_id", petId) }
        view.findViewById<FloatingActionButton>(R.id.fab_add_medication).setOnClickListener {
            findNavController().navigate(R.id.action_medication_to_medicationForm, bundle)
        }

        loadMedications(petId)
    }

    private fun setupPlaceholders(items: List<Medication>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_medication)
        if (items.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadMedications(petId: String) {
        medications = medicationService.findAllByPetId(petId)
        medications.observe(viewLifecycleOwner) { items ->
            recyclerView.adapter = MedicationAdapter(
                medications = items,
                onClick = { medication ->
                    MedicationDetailsBottomSheet.newInstance(medication)
                        .show(parentFragmentManager, "MedicationDetailsBottomSheet")
                },
                onDeleteClick = { medication ->
                    SwipeDeleteHelper.confirmDelete(
                        fragment = this,
                        message = "Are you sure you want to delete ${medication.name}?"
                    ) {
                        medicationService.deleteById(medication.id)
                        SnackbarUtil.showSuccess(
                            view = requireView(),
                            title = "Success",
                            message = "Record has been deleted"
                        )
                    }
                }
            )
            setupPlaceholders(items)
        }
    }
}
