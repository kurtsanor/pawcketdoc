package com.example.pawcketdoc.ui

import android.app.AlertDialog
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.AppointmentAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.service.AppointmentService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.launch

class AppointmentFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var appointmentService: AppointmentService
    private lateinit var recyclerView: RecyclerView
    private lateinit var appointments: LiveData<List<Appointment>>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_appointment, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Appointments"
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = view.findViewById< MaterialCalendarView>(R.id.calendarAppointments)
        val today = CalendarDay.today()
        calendarView.setDateSelected(today, true)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        appointmentService = AppointmentService(db.appointmentDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewAppointments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val buttonAdd = view.findViewById<Button>(R.id.buttonAddAppointment)

        val bundle = Bundle().apply {
            putString("pet_id", petId)
        }

        buttonAdd.setOnClickListener {
            findNavController().navigate(R.id.action_appointment_to_appointmentForm, bundle)
        }

        loadAppointments(petId)
        setupSwipeHandler()
    }

    fun setupPlaceholders (appointments: List<Appointment>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_appointment)
        if (appointments.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun loadAppointments(petId: String) {
        appointments = appointmentService.findAllByPetId(petId)
        appointments.observe(viewLifecycleOwner) { appointments ->
            recyclerView.adapter = AppointmentAdapter(appointments) { appointment ->
                val bottomSheet = AppointmentDetailsBottomSheet.newInstance(appointment)
                bottomSheet.show(parentFragmentManager, "AppointmentDetailsBottomSheet")
            }
            setupPlaceholders(appointments)
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
                val appointment = appointments.value?.get(position)

                if (appointment == null) {
                    return
                }


                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete ${appointment?.title}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                appointmentService.deleteById(appointment.id)
                                SnackbarUtil.showSuccess(
                                    view = requireView(),
                                    title = "Success",
                                    message = "Appointment has been deleted"
                                )
                                dialog.dismiss()
                            } catch (e: Exception) {
                                SnackbarUtil.showError(
                                    view = requireView(),
                                    title = "Error",
                                    message = e.message.toString()
                                )
                            }
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