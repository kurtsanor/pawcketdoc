package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.VaccinationAdapter
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.database.toModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class VaccinationFragment : Fragment() {

    private var petId: Long = -1L
    private lateinit var adapter: VaccinationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_vaccination, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Vaccination"

        //  get petId
        petId = arguments?.getLong("petId", -1L) ?: -1L
        if (petId == -1L) {
            Toast.makeText(requireContext(), "Pet ID missing.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // Calendar
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setDateSelected(CalendarDay.today(), true)

        // Recycler
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVaccination)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = VaccinationAdapter(mutableListOf())
        recyclerView.adapter = adapter

        //  Observe Room LiveData
        val db = DatabaseProvider.getDatabase(requireContext())
        db.vaccinationDao().observeVaccinationsByPetId(petId).observe(viewLifecycleOwner) { entities ->
            adapter.updateVaccinations(entities.map { it.toModel() })
        }

        // Add button → open form (pass petId)
        view.findViewById<Button>(R.id.buttonAddVaccine).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainerView2,
                    VaccinationFormFragment().apply {
                        arguments = Bundle().apply { putLong("petId", petId) }
                    }
                )
                .addToBackStack(null)
                .commit()
        }
    }
}
