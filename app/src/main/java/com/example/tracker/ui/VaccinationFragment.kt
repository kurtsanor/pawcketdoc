package com.example.tracker.ui

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
import com.example.tracker.R
import com.example.tracker.adapter.PetAdapter
import com.example.tracker.adapter.VaccinationAdapter
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.model.Pet
import com.example.tracker.model.Vaccination
import com.example.tracker.service.VaccinationService
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.time.LocalDate

class VaccinationFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var vaccinationService: VaccinationService
    private lateinit var recyclerView: RecyclerView
    private lateinit var vaccinationList: LiveData<List<Vaccination>>

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
        vaccinationService = VaccinationService(db.vaccinationDao())

        val calendarView = view.findViewById< MaterialCalendarView>(R.id.calendarView)
        val today = CalendarDay.today()
        calendarView.setDateSelected(today, true)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVaccination)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val buttonAdd = view.findViewById<Button>(R.id.buttonAddVaccine)



        val bundle = Bundle().apply {
            putLong("pet_id", arguments?.getLong("pet_id", -1L) ?: -1L)
        }

        loadVaccinations(bundle.getLong("pet_id"))
        setupSwipeHandler()

        buttonAdd.setOnClickListener {
            findNavController().navigate(R.id.action_vaccinations_to_vaccinationForm, bundle)
        }
    }

    fun loadVaccinations(petId: Long) {
        vaccinationList = vaccinationService.findAllByPetId(petId)

        vaccinationList.observe(viewLifecycleOwner) { vaccinations ->
            recyclerView.adapter = VaccinationAdapter(vaccinations) { vaccination ->
                val bottomSheet = VaccinationDetailsBottomSheet()
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
                                Toast.makeText(requireContext(), "${vaccination?.name} deleted", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Swiped ${vaccination?.name} to $dir", Toast.LENGTH_SHORT).show()

            }
        }
        // Attach the swipe handler to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


}