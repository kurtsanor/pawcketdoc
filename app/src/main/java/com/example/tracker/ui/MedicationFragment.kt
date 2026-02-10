package com.example.tracker.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.MedicalRecordAdapter
import com.example.tracker.adapter.MedicationAdapter
import com.example.tracker.model.Medication
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate

class MedicationFragment : Fragment() {

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Medication"

        val mockData = listOf(
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor"),
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor"),
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor"),
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor"),
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor"),
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor"),
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor"),
            Medication(null, 0, "Amoxicillin", "50mg", "Twice/day", LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 2, 2), "For infection", "Lorem ipsum dolor")
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMedications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = MedicationAdapter(mockData)

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_medication)

        fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, MedicationFormFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}