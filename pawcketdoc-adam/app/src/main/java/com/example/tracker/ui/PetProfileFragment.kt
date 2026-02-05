package com.example.tracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_CLOSE
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.example.tracker.R

class PetProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pet_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Pet Profile"

        val buttonVaccination = view.findViewById< Button>(R.id.buttonVaccination)
        buttonVaccination.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, VaccinationFragment())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

        val buttonMedicalHistory = view.findViewById< Button>(R.id.buttonMedicalHistory)
        buttonMedicalHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, MedicalHistoryFragment())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

        val buttonGrowth = view.findViewById< Button>(R.id.buttonGrowth)
        buttonGrowth.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, GrowthFragment())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

        val buttonDocuments = view.findViewById< Button>(R.id.buttonDocuments)
        buttonDocuments.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, DocumentsFragment())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

        val buttonAppointments = view.findViewById< Button>(R.id.buttonAppointments)
        buttonAppointments.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, AppointmentFragment())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

        val buttonMedications = view.findViewById< Button>(R.id.buttonMedications)
        buttonMedications.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, MedicationFragment())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

    }



}