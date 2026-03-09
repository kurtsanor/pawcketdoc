package com.example.pawcketdoc.ui

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.VaccinationAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.Vaccination
import com.example.pawcketdoc.service.VaccinationService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.launch
import java.lang.RuntimeException

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
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vaccination, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Vaccinations"
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

        val calendarView = view.findViewById< MaterialCalendarView>(R.id.calendarView)
        val today = CalendarDay.today()
        calendarView.setDateSelected(today, true)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVaccination)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val buttonAdd = view.findViewById<Button>(R.id.buttonAddVaccine)

        val bundle = Bundle().apply {
            putString("pet_id", arguments?.getString("pet_id"))
        }

        loadVaccinations(bundle.getString("pet_id")!!)
        setupSwipeHandler()

        buttonAdd.setOnClickListener {
            findNavController().navigate(R.id.action_vaccinations_to_vaccinationForm, bundle)
        }
    }

    fun loadVaccinations(petId: String) {
        vaccinationList = vaccinationService.findAllByPetId(petId)

        vaccinationList.observe(viewLifecycleOwner) { vaccinations ->
            recyclerView.adapter = VaccinationAdapter(vaccinations) { vaccination ->
                val bottomSheet = VaccinationDetailsBottomSheet.newInstance(vaccination)
                bottomSheet.show(parentFragmentManager, "VaccinationDetailsBottomSheet")
            }
            setupPlaceholders(vaccinations)
        }
    }

    fun setupPlaceholders (vaccinationList: List<Vaccination>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_vaccinations)
        if (vaccinationList.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
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
                val vaccination = vaccinationList.value?.get(position)

                if (vaccination == null) {
                    return
                }


                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete ${vaccination?.name}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                vaccinationService.deleteById(vaccination.id)

                                // User confirmed, remove pet from list then update adapter
//                                petList.removeAt(position)
//                                recyclerView.adapter?.notifyItemRemoved(position)
                                SnackbarUtil.showSuccess(
                                    view = requireView(),
                                    title = "Success",
                                    message = "Record has been deleted"
                                )
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
            }
        }
        // Attach the swipe handler to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


}