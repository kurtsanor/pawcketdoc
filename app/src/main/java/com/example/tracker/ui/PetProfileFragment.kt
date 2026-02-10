package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.example.tracker.R
import com.example.tracker.database.DatabaseProvider
import java.time.LocalDate
import java.time.Period

class PetProfileFragment : Fragment() {

    private var petId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pet_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Pet Profile"

        //  Get petId from arguments
        petId = arguments?.getLong("petId", -1L) ?: -1L
        if (petId == -1L) {
            Toast.makeText(requireContext(), "Pet ID not found.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        //  Update profile card using LiveData
        observePetAndBindToProfileCard(view)

        // Buttons (petId will be passed automatically by openFragment)
        view.findViewById<Button>(R.id.buttonVaccination).setOnClickListener {
            openFragment(VaccinationFragment())
        }
        view.findViewById<Button>(R.id.buttonMedicalHistory).setOnClickListener {
            openFragment(MedicalHistoryFragment())
        }
        view.findViewById<Button>(R.id.buttonGrowth).setOnClickListener {
            openFragment(GrowthFragment())
        }
        view.findViewById<Button>(R.id.buttonDocuments).setOnClickListener {
            openFragment(DocumentsFragment())
        }
        view.findViewById<Button>(R.id.buttonAppointments).setOnClickListener {
            openFragment(AppointmentFragment())
        }
        view.findViewById<Button>(R.id.buttonMedications).setOnClickListener {
            openFragment(MedicationFragment())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observePetAndBindToProfileCard(root: View) {
        //  Add id="profileCardContainer" in XML for the LinearLayout inside profile card
        val container = root.findViewById<LinearLayout>(R.id.profileCardContainer)

        val tvName = container.getChildAt(1) as? TextView
        val tvAgeGender = container.getChildAt(2) as? TextView
        val tvBreedColor = container.getChildAt(3) as? TextView

        if (tvName == null || tvAgeGender == null || tvBreedColor == null) {
            Toast.makeText(requireContext(), "Profile UI not found. Please check layout.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(requireContext())
        db.petDao().observePetById(petId).observe(viewLifecycleOwner) { pet ->
            if (pet == null) {
                Toast.makeText(requireContext(), "Pet not found.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
                return@observe
            }

            tvName.text = pet.name
            tvAgeGender.text = "${formatAgeFromBirthDate(pet.birthDate)}, ${pet.gender}"
            tvBreedColor.text = pet.breed
        }
    }

    private fun openFragment(fragment: Fragment) {
        //  Pass petId to every child fragment automatically
        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
            putLong("petId", petId)
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView2, fragment)
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatAgeFromBirthDate(birthDate: LocalDate): String {
        val today = LocalDate.now()
        if (birthDate.isAfter(today)) return "0 months"

        val p = Period.between(birthDate, today)
        val years = p.years
        val months = p.months

        return when {
            years <= 0 && months <= 1 -> "1 month"
            years <= 0 -> "$months months"
            months == 0 && years == 1 -> "1 year"
            months == 0 -> "$years years"
            years == 1 -> "1 year, $months months"
            else -> "$years years, $months months"
        }
    }
}
