package com.example.pawcketdoc.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.VaccinationAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.Vaccination
import com.example.pawcketdoc.service.VaccinationService
import com.example.pawcketdoc.util.SnackbarUtil
import com.example.pawcketdoc.util.SwipeDeleteHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class VaccinationFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var vaccinationService: VaccinationService
    private lateinit var recyclerView: RecyclerView
    private lateinit var vaccinationList: LiveData<List<Vaccination>>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_vaccination, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Vaccinations"
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
        vaccinationService = VaccinationService(db.vaccinationDao(), firebaseFirestore, firebaseAuth)

        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setDateSelected(CalendarDay.today(), true)

        recyclerView = view.findViewById(R.id.recyclerViewVaccination)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val bundle = Bundle().apply { putString("pet_id", arguments?.getString("pet_id")) }
        loadVaccinations(bundle.getString("pet_id")!!)

        view.findViewById<Button>(R.id.buttonAddVaccine).setOnClickListener {
            findNavController().navigate(R.id.action_vaccinations_to_vaccinationForm, bundle)
        }
    }

    private fun loadVaccinations(petId: String) {
        vaccinationList = vaccinationService.findAllByPetId(petId)
        vaccinationList.observe(viewLifecycleOwner) { vaccinations ->
            recyclerView.adapter = VaccinationAdapter(
                vaccinations = vaccinations,
                onClick = { vaccination ->
                    VaccinationDetailsBottomSheet.newInstance(vaccination)
                        .show(parentFragmentManager, "VaccinationDetailsBottomSheet")
                },
                onDeleteClick = { vaccination ->
                    SwipeDeleteHelper.confirmDelete(
                        fragment = this,
                        message = "Are you sure you want to delete ${vaccination.name}?"
                    ) {
                        vaccinationService.deleteById(vaccination.id)
                        SnackbarUtil.showSuccess(
                            view = requireView(),
                            title = "Success",
                            message = "Record has been deleted"
                        )
                    }
                }
            )
            setupPlaceholders(vaccinations)
        }
    }

    private fun setupPlaceholders(vaccinations: List<Vaccination>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_vaccinations)
        if (vaccinations.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
