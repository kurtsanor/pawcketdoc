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
import androidx.navigation.fragment.findNavController
import com.example.tracker.R

class PetProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pet_profile, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Pet Profile"
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val petId: Long? = arguments?.getLong("pet_id", -1L)

        val bundle = Bundle().apply {
            putLong("pet_id", petId ?: -1L)
        }

        val buttonVaccination = view.findViewById< Button>(R.id.buttonVaccination)
        buttonVaccination.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_vaccinations, bundle)
        }

        val buttonMedicalHistory = view.findViewById< Button>(R.id.buttonMedicalHistory)
        buttonMedicalHistory.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_medicalHistory, bundle)
        }

        val buttonGrowth = view.findViewById< Button>(R.id.buttonGrowth)
        buttonGrowth.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_growth, bundle)
        }

        val buttonDocuments = view.findViewById< Button>(R.id.buttonDocuments)
        buttonDocuments.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_documents, bundle)
        }

        val buttonAppointments = view.findViewById< Button>(R.id.buttonAppointments)
        buttonAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_appointments, bundle)
        }

        val buttonMedications = view.findViewById< Button>(R.id.buttonMedications)
        buttonMedications.setOnClickListener {
            findNavController().navigate(R.id.action_petProfile_to_medications, bundle)
        }

    }

}